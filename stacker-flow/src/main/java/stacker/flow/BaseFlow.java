package stacker.flow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stacker.common.*;
import stacker.common.dto.Command;


import static org.junit.Assert.*;

/**
 * @param <A> argument type
 * @param <R> returns type
 * @param <F> flowData type
 */
public abstract class BaseFlow<A, R, F> {
    private static final Logger log = LoggerFactory.getLogger(BaseFlow.class);

    private final Contract<A, R> flowContract;
    private final Class<F> flowDataClass;
    private final IParser flowDataParser;

    private final Map<String, BaseState<? super F>> states = new HashMap<>();
    private final Map<String, ResourceRequestHandler<? super F>> resourceHandlers = new HashMap<>();
    private final Map<Command.Type, IHandler<Command, F>>
            incomingHandlers = new HashMap<>();

    {
        incomingHandlers.put(Command.Type.OPEN, (command, context) ->
                start(parseRq(command.getContentBody()), context));

        incomingHandlers.put(Command.Type.ANSWER, (command, context) ->
                getInteractiveState(command.getState())
                        .handle(command.getContentBody(), context));

        incomingHandlers.put(Command.Type.RETURN, (command, context) ->
                getInteractiveState(command.getState())
                        .handle(command.getContentBody(), context));
    }

    protected BaseFlow(
            Contract<A, R> contract,
            Class<F> flowDataClass,
            IParser flowDataParser) {

        flowContract = contract;
        this.flowDataClass = flowDataClass;
        this.flowDataParser = flowDataParser;

        this.configure();
        this.validate();
    }

    protected abstract void configure();

    protected abstract boolean isDaemon();

    protected abstract F createFlowData(A arg);

    protected abstract R makeReturn(FlowContext<F> context);

    protected abstract void onStart(FlowContext<F> context);

    private void start(A argument, FlowContext<F> context) {
        F flowData = createFlowData(argument);
        FlowContext<F> newContext =
                new FlowContext<>(
                        this,
                        context.getFlowName(),
                        context.getStateName(),
                        flowData,
                        context.getCallback());
        onStart(newContext);
    }

    public final Contract getContract() {
        return flowContract;
    }

    protected final void addState(String name, BaseState<? super F> state) {
        assertNotNull("The NAME should not be null", name);
        name = name.trim().toUpperCase();
        assertNotEquals("The NAME should not be empty string", name, "");
        assertFalse("State with name '" + name + "' already registered", states.containsKey(name));
        assertNotNull("State should not be null", state);

        states.put(name, state);
        state.setFlow(this);
    }

    private BaseState<? super F> getState(String name) {
        return states.get(name.trim().toUpperCase());
    }

    private InteractiveState<?, ?, ? super F, ?> getInteractiveState(String name) throws Exception {
        BaseState<? super F> state = getState(name);
        if (state instanceof InteractiveState) {
            return (InteractiveState<?, ?, ? super F, ?>) state;
        }
        throw new Exception("State with name " + name + " is not interactive");
    }

    protected final void addResourceRequestHandler(String path, ResourceRequestHandler<? super F> handler) {
        assertNotNull("Path could not be null", path);
        path = path.trim();
        assertNotEquals("Path could not be empty", "", path);
        assertNotNull("Handler could not be null", handler);
        assertFalse("Path \"" + path + "\" already used", resourceHandlers.containsKey(path));
        resourceHandlers.put(path, handler);
        log.info("Resource handler for \"" + path + "\" added");
    }

    private ResourceRequestHandler<F> getResourceRequestHandler(String path) {
        //TODO implement this
        return null;
    }

    /**
     * This method will be called by server to handle request
     *
     * @param command  - the Command that server receive
     * @param callback - the server callback
     */
    public void handleCommand(Command command, ICallback<Command> callback) {

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
            log.error("Error handling request", e);
            callback.reject(e);
        }
    }

    protected void enterState(String name, FlowContext<F> context) {
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
        state.onEnter(transContext);
    }

    private A parseRq(byte[] rqString) throws ParsingException {
        return flowContract.getParser().parse(rqString, flowContract.getQuestionType());
    }

    private F parseFlowData(byte[] flowData) throws ParsingException {
        return flowDataParser.parse(flowData, flowDataClass);
    }

    byte[] serializeFlowData(Object o) throws SerializingException {
        return flowDataParser.serialize(o);
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
            for (Enum<?> e : exits) {
                String target;
                try {
                    target = getInteractiveState(key).getTransition(e);
                } catch (Exception e1) {
                    continue;
                }
                if (target == null) {
                    throw new IllegalStateException(
                            "Misconfiguration:\n exit with name \"" +
                                    e.name() + "\" from State \"" + key +
                                    "\" have no destination target");
                }
                targets.add(target);
            }
        }
        for (String key : states.keySet()) {
            if (!targets.contains(key))
                log.info("State " + key + " is unreachable");
        }
    }

}