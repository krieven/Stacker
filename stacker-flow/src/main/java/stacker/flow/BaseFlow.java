package stacker.flow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stacker.common.*;


import static org.junit.Assert.*;

/**
 * @param <A> argument type
 * @param <R> returns type
 * @param <F> flow data type
 */
public abstract class BaseFlow<A, R, F> {
    private static final Logger log = LoggerFactory.getLogger(BaseFlow.class);

    private final Contract<A, R> flowContract;
    private final Class<F> flowDataClass;
    private final IParser flowDataParser;

    private final Map<String, BaseState<? super F, ?>> states = new HashMap<>();


    public BaseFlow(
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

    protected final void addState(String name, BaseState<? super F, ?> state) {
        assertNotNull("The NAME should not be null", name);
        name = name.trim().toUpperCase();
        assertNotEquals("The NAME should not be empty string", name, "");
        assertFalse("State with name '" + name + "' already registered", states.containsKey(name));
        assertNotNull("State should not be null", state);

        states.put(name, state);
    }

    private BaseState<? super F, ?> getState(String name) {
        return states.get(name.trim().toUpperCase());
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

    private final Map<Command.Type, IHandler<Command, F>>
            incomingHandlers = new HashMap<>();

    {
        incomingHandlers.put(Command.Type.OPEN, (command, context) ->
                start(parseRq(command.getContentBody()), context));

        incomingHandlers.put(Command.Type.ANSWER, (command, context) ->
                getState(command.getState())
                        .handle(command.getContentBody(), context));

        incomingHandlers.put(Command.Type.RETURN, (command, context) ->
                getState(command.getState())
                        .handle(command.getContentBody(), context));
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
        BaseState<? super F, ?> state = getState(name);
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
            Enum<?>[] exits = getState(key).getExits();
            if (exits == null) continue;
            for (Enum<?> e : exits) {
                String target = getState(key).getTransition(e);
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