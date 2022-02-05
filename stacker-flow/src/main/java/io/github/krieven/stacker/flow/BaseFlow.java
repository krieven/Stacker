package io.github.krieven.stacker.flow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.krieven.stacker.common.ICallback;
import io.github.krieven.stacker.common.IParser;
import io.github.krieven.stacker.common.ParsingException;
import io.github.krieven.stacker.common.SerializingException;
import io.github.krieven.stacker.flow.resource.ResourceLeaf;
import io.github.krieven.stacker.flow.resource.ResourceTree;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.krieven.stacker.common.dto.Command;


import static org.junit.Assert.*;

/**
 * Base class for any plain Workflow.
 * Each Workflow implements Role part of BPMN schema,
 * can receive argument of type Q and return some value of type A.
 *
 * Workflow consists of States,
 * all data, that should be passed trough Workflow between states,
 * should be stored in the instance of your FlowData class.
 *
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

    /**
     * Constructor of BaseFlow, you should call it in the constructor of your concrete Workflow class.
     *
     * @param contract       - the contract of your Workflow - Question type, Answer type and serializing format,
     *                       defined by parser.
     * @param flowDataClass  - the class of the FlowData
     * @param flowDataParser - parser of FlowData
     */
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
     * This method should be called by server to handle command
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
                        command.getProperties(),
                        callback);

        IHandler<Command, F> handler =
                incomingHandlers.get(command.getType());

        try {
            handler.handle(command, context);
        } catch (Exception e) {
            log.error("Error handling request", e);
            callback.reject(e);
        }
    }

    public final Contract getContract() {
        return flowContract;
    }

    /**
     * In this method you should configure Workflow schema by adding States and defining transitions
     */
    protected abstract void configure();

    /**
     * Sometimes the same Answer of some Workflow needed many times.
     * If your Workflow should store its FlowData in the context of current user session
     * for next call, this method should return true, otherwise it should return false.
     *
     * @param context - the context of this flow
     * @return boolean true, if FlowData should be stored for current user session
     */
    protected abstract boolean isDaemon(FlowContext<F> context);

    /**
     * When the Workflow starts, it requires the FlowData to store collected data
     *
     * @param arg     - the Question on which this Workflow should answer
     * @param context - the context of the current Workflow
     * @return new instance of your FlowData for the current call of Workflow
     */
    protected abstract F createFlowData(Q arg, FlowContext<F> context);

    /**
     * This method should extract Answer of current Workflow from its context
     *
     * @param context - the context of the current Workflow
     * @return the instance of Answer class or null
     */
    protected abstract A makeReturn(FlowContext<F> context);

    /**
     * When Workflow starts, you should determine - which State should be entered firstly
     *
     * @param context - the context of the current Workflow
     * @return StateCompletion, it can be taked from enterState method
     */
    @NotNull
    protected abstract StateCompletion onStart(FlowContext<F> context);

    /**
     *
     * @param name - the name of the entering State in the schema
     * @param context - the context of this Workflow
     * @return StateCompletion
     */
    @NotNull
    protected StateCompletion enterState(String name, FlowContext<F> context) {
        FlowContext<F> transContext =
                new FlowContext<>(
                        this,
                        context.getFlowName(),
                        name,
                        context.getFlowData(),
                        context.getProperties(),
                        context.getCallback()
                );
        BaseState<? super F> state = getState(name);
        if (state == null) {
            throw new IllegalStateException("State \"" + name + "\" was not found");
        }
        return state.onEnter(transContext);
    }

    /**
     * Adds State to Workflow schema
     *
     * @param name - the name of adding State
     * @param state - the instance of adding State class
     */
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
            flowData = createFlowData(argument, context);
        }
        FlowContext<F> newContext =
                new FlowContext<>(
                        this,
                        context.getFlowName(),
                        context.getStateName(),
                        flowData,
                        context.getProperties(),
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