package stacker.flow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stacker.common.*;
import stacker.common.dto.Command;
import stacker.flow.resource.ResourceLeaf;
import stacker.flow.resource.ResourceTree;


import static org.junit.Assert.*;

/**
 * @param <Q> argument type
 * @param <A> returns type
 * @param <F> flowData type
 */
public abstract class BaseFlow<Q, A, F> {
    private static final Logger log = LoggerFactory.getLogger(BaseFlow.class);

    private final Map<String, ResourceTree<ResourceController<? super F>>>
            resourceControllers = new HashMap<>();

    private final Contract<Q, A> flowContract;
    private final Class<F> flowDataClass;
    private final IParser flowDataParser;

    private final Map<String, BaseState<? super F>> states = new HashMap<>();
    private final Map<Command.Type, IHandler<Command, F>> incomingHandlers = new HashMap<>();

    {
        incomingHandlers.put(Command.Type.OPEN, (command, context) ->
                start(parseRq(command.getContentBody()), context));

        incomingHandlers.put(Command.Type.ANSWER, (command, context) ->
                getState(command.getState())
                        .handle(command.getContentBody(), context));

        incomingHandlers.put(Command.Type.RETURN, (command, context) ->
                getState(command.getState())
                        .handle(command.getContentBody(), context));

        incomingHandlers.put(Command.Type.RESOURCE, new HandlerResource<>());
    }

    protected BaseFlow(
            Contract<Q, A> contract,
            Class<F> flowDataClass,
            IParser flowDataParser) {

        flowContract = contract;
        this.flowDataClass = flowDataClass;
        this.flowDataParser = flowDataParser;

        this.configure();
        this.validate();
    }

    /**
     * This method will be called by server to handle command
     *
     * @param command  - the Command that server receive
     * @param callback - the server callback
     */
    public final void handleCommand(@NotNull Command command, @NotNull ICallback<Command> callback) {

        F flowData = null;
        if (command.getFlowData() != null) {
            try {
                flowData = parseFlowData(command.getFlowData());
            } catch (ParsingException e) {
                log.error("Error parsing request", e);
                callback.reject(e);
                return;
            }
        }

        FlowContext<F> context =
                new FlowContext<>(
                        this,
                        command.getFlow(),
                        command.getState(),
                        flowData,
                        callback);

        IHandler<Command, F> handler =
                incomingHandlers.get(command.getType());

        try {
            handler.handle(command, context);
        } catch (Exception e) {
            //todo change it to ERROR(IllegalArgument)
            log.error("Error handling request", e);
            callback.reject(e);
        }
    }

    public final Contract getContract() {
        return flowContract;
    }

    protected abstract void configure();

    protected abstract boolean isDaemon();

    protected abstract F createFlowData(Q arg);

    protected abstract A makeReturn(FlowContext<F> context);

    @NotNull
    protected abstract StateCompletion onStart(FlowContext<F> context);

    @NotNull
    protected StateCompletion enterState(String name, FlowContext<F> context) {
        FlowContext<F> transContext =
                new FlowContext<>(
                        this,
                        context.getFlowName(),
                        name,
                        context.getFlowData(),
                        context.getCallback()
                );
        BaseState<? super F> state = getState(name);
        if (state == null) {
            throw new IllegalStateException("State \"" + name + "\" was not found");
        }
        return state.onEnter(transContext);
    }

    protected void addState(String name, BaseState<? super F> state) {
        assertNotNull("The NAME should not be null", name);
        name = name.trim().toUpperCase();
        assertNotEquals("The NAME should not be empty string", name, "");
        assertFalse("State with name '" + name + "' already registered", states.containsKey(name));
        assertNotNull("State should not be null", state);
        assertFalse("this State already added into the Flow", states.values().contains(state));

        states.put(name, state);
        state.setName(name);

        if (state.resourceControllers.size() > 0) {
            ResourceTree<ResourceController<? super F>> handlers = new ResourceTree<>();
            resourceControllers.put(name, handlers);
            state.resourceControllers.forEach((key, value) ->
                    addResourceController(handlers, key, value)
            );
        }
    }

    byte[] serializeFlowData(Object o) throws SerializingException {
        return flowDataParser.serialize(o);
    }

    private void start(Q argument, FlowContext<F> context) {
        F flowData = context.getFlowData();
        if (flowData == null) {
            flowData = createFlowData(argument);
        }
        FlowContext<F> newContext =
                new FlowContext<>(
                        this,
                        context.getFlowName(),
                        context.getStateName(),
                        flowData,
                        context.getCallback()
                );
        onStart(newContext).doCompletion();
    }

    private BaseState<? super F> getState(String name) {
        return states.get(name.trim().toUpperCase());
    }

    @SuppressWarnings("unchecked")
    private StateInteractive<?, ?, ? super F, ?> getInteractiveState(String name) throws ClassCastException {
        BaseState<? super F> state = getState(name);
        return (StateInteractive<?, ?, ? super F, ?>) state;
    }

    private Q parseRq(byte[] rq) throws ParsingException {
        return flowContract.getParser().parse(rq, flowContract.getQuestionType());
    }

    private F parseFlowData(byte[] flowData) throws ParsingException {
        return flowDataParser.parse(flowData, flowDataClass);
    }

    private void validate() {
        List<String> targets = new ArrayList<>();
        if (states.keySet().isEmpty()) {
            log.error("No states are defined");
        }
        for (String key : states.keySet()) {
            Enum<?>[] exits;
            try {
                exits = getInteractiveState(key).getExits();
            } catch (Exception e) {
                continue;
            }
            if (exits == null || exits.length == 0) {
                throw new IllegalStateException("StateInteractive " + key + " should have exits");
            }
            for (Enum<?> exit : exits) {
                String target;
                try {
                    target = getInteractiveState(key).getTransition(exit);
                } catch (Exception e) {
                    continue;
                }
                if (target == null) {
                    throw new IllegalStateException(
                            "Misconfiguration:\n exit with name \"" +
                                    exit.name() + "\" from State \"" + key +
                                    "\" have no destination target");
                }
                targets.add(target);
            }
        }
        boolean hasTerminator = false;
        for (String key : states.keySet()) {
            if (!targets.contains(key))
                log.warn("State " + key + " is unreachable");
            if (getState(key) instanceof StateTerminator)
                hasTerminator = true;
        }
        if (!hasTerminator) {
            throw new IllegalStateException(
                    "Misconfiguration:\n Flow have no Terminator");
        }
    }

    private void addResourceController(ResourceTree<ResourceController<? super F>> tree, String path, ResourceController<? super F> handler) {
        assertNotNull("Path could not be null", path);
        path = path.trim();
        assertNotEquals("Path could not be empty", "", path);
        assertNotNull("Handler could not be null", handler);

        tree.add(path, handler);

        log.info("Resource handler for \"" + path + "\" added");
    }

    ResourceLeaf<ResourceController<? super F>> getResourceLeaf(String stateName, String path) {
        stateName = stateName.trim().toUpperCase();
        if (!resourceControllers.containsKey(stateName)) {
            return null;
        }
        return resourceControllers.get(stateName).find(path);
    }
}