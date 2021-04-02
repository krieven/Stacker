package stacker.flow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stacker.common.*;


import static org.junit.Assert.*;

public abstract class BaseFlow<ArgumentT, ReturnT, FlowDataT, ResourcesT> {
    private static Logger log = LoggerFactory.getLogger(BaseFlow.class);

    private TheContract<ArgumentT, ReturnT> flowContract;
    private Class<FlowDataT> flowDataClass;
    private IParser flowDataParser;

    private ResourcesT resources;

    private Map<String, BaseState<? super FlowDataT, ? super ResourcesT, ?>> states = new HashMap<>();


    public BaseFlow(
            TheContract<ArgumentT, ReturnT> contract,
            Class<FlowDataT> flowDataClass,
            IParser flowDataParser,
            ResourcesT resources) {

        flowContract = contract;
        this.flowDataClass = flowDataClass;
        this.flowDataParser = flowDataParser;
        this.resources = resources;


        this.configure();
        this.validate();
    }

    protected abstract void configure();

    protected abstract FlowDataT createFlowData(ArgumentT arg);

    protected abstract ReturnT makeReturn(FlowContext<FlowDataT, ResourcesT> context);

    protected abstract void onStart(FlowContext<FlowDataT, ResourcesT> context);

    private void start(ArgumentT argument, FlowContext<FlowDataT, ResourcesT> context) {
        FlowDataT flowData = createFlowData(argument);
        FlowContext<FlowDataT, ResourcesT> newContext =
                new FlowContext<>(
                        this,
                        context.getFlowName(),
                        context.getStateName(),
                        flowData,
                        context.getResources(),
                        context.getCallback());
        onStart(newContext);
    }

    public final TheContract getContract() {
        return flowContract;
    }

    protected final void addState(String name, BaseState<? super FlowDataT, ? super ResourcesT, ?> state) {
        assertNotNull("The NAME should not be null", name);
        name = name.trim().toUpperCase();
        assertNotEquals("The NAME should not be empty string", name, "");
        assertFalse("ActionState with name '" + name + "' already registered", states.containsKey(name));
        assertNotNull("ActionState should not be null", state);

        states.put(name, state);
    }

    private BaseState<? super FlowDataT, ? super ResourcesT, ?> getState(String name) {
        return states.get(name.trim().toUpperCase());
    }


    /**
     * This method will be called by server to handle request
     *
     * @param command  - the Command that server receive
     * @param callback - the server callback
     */
    public void handleCommand(Command command, ICallback<Command> callback) {

        FlowDataT flowData = null;
        if (command.getFlowData() != null) {
            try {
                flowData = parseFlowData(command.getFlowData());
            } catch (ParsingException e) {
                callback.reject(e);
                return;
            }
        }

        FlowContext<FlowDataT, ResourcesT> context =
                new FlowContext<>(
                        this,
                        command.getFlow(),
                        command.getState(),
                        flowData,
                        resources,
                        callback);

        IHandler<Command, FlowDataT, ResourcesT> handler =
                incomingHandlers.get(command.getType());

        try {
            handler.handle(command, context);
        } catch (Exception e) {
            callback.reject(e);
        }
    }

    private Map<Command.Type, IHandler<Command, FlowDataT, ResourcesT>>
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


    protected void sendTransition(String name, FlowContext<FlowDataT, ResourcesT> context) {
        FlowContext<FlowDataT, ResourcesT> transContext =
                new FlowContext<>(
                        this,
                        context.getFlowName(),
                        name,
                        context.getFlowData(),
                        context.getResources(),
                        context.getCallback()
                );
        getState(name).onEnter(transContext);
    }

    private ArgumentT parseRq(byte[] rqString) throws ParsingException {
        return flowContract.getParser().parse(rqString, flowContract.getArgumentClass());
    }

    private FlowDataT parseFlowData(byte[] flowData) throws ParsingException {
        return flowDataParser.parse(flowData, flowDataClass);
    }

    byte[] serializeFlowData(Object o) throws SerializingException {
        return flowDataParser.serialize(o);
    }

    private void validate() {
        List<String> targets = new ArrayList<>();
        for (String key : states.keySet()) {
            Enum<?>[] exits = states.get(key).getExits();
            if (exits == null) continue;
            for (Enum<?> e : exits) {
                String target = states.get(key).getTransition(e);
                if (target == null) {
                    throw new RuntimeException(
                            "Misconfiguration:\n exit with name \"" +
                                    e.name() + "\" from state \"" + key +
                                    "\" have no destination target");
                }
                targets.add(target);
            }
        }
        for (String key : states.keySet()) {
            if (!targets.contains(key))
                log.info("state " + key + " is probably unreachable");
        }
    }

}