package stacker.flow;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stacker.common.*;


import static org.junit.Assert.*;

public abstract class Flow<ArgumentT, ReturnT, FlowDataT, DaemonDataT, ResourcesT> {
    private static Logger log = LoggerFactory.getLogger(Flow.class);

    private FlowContract<ArgumentT, ReturnT> flowContract;
    private Class<FlowDataT> flowDataClass;
    private Class<DaemonDataT> daemonDataClass;
    private IParser flowDataParser;

    private ResourcesT resources;

    private Map<String, AState<FlowDataT, DaemonDataT, ResourcesT>> states = new HashMap<>();


    public Flow(
            @NotNull FlowContract<ArgumentT, ReturnT> contract,
            @NotNull Class<FlowDataT> flowDataClass,
            @NotNull Class<DaemonDataT> daemonDataClass,
            @NotNull IParser flowDataParser,
            ResourcesT resources) {
        flowContract = contract;
        this.flowDataClass = flowDataClass;
        this.daemonDataClass = daemonDataClass;
        this.flowDataParser = flowDataParser;
        this.resources = resources;
        this.configure();
        this.validate();
    }

    public abstract void configure();

    public abstract FlowDataT createFlowData();

    public abstract DaemonDataT createDaemonData();

    public abstract ReturnT makeReturn(RequestContext<FlowDataT, DaemonDataT, ResourcesT> context);

    public abstract void onOpen(ArgumentT arg, RequestContext<FlowDataT, DaemonDataT, ResourcesT> context);

    public final FlowContract getContract() {
        return flowContract;
    }

    public final void addState(String name, AState<FlowDataT, DaemonDataT, ResourcesT> state) {
        assertNotNull("The NAME should not be null", name);
        name = name.trim().toUpperCase();
        assertNotEquals("The NAME should not be empty string", name, "");
        assertFalse("ActionState with name '" + name + "' already registered", states.containsKey(name));
        assertNotNull("ActionState should not be null", state);

        states.put(name, state);
    }

    private AState<FlowDataT, DaemonDataT, ResourcesT> getState(String name) {
        return states.get(name.trim().toUpperCase());
    }

    /**
     * This method will be called by server to handle request
     *
     * @param command  - the Command that server receive
     * @param callback - the server callback
     */
    public final void handleCommand(Command command, ICallback<Command> callback) {

        FlowDataT flowData;
        DaemonDataT daemonData;
        try {
            flowData = parseFlowData(command.getFlowData());
            daemonData = parseDaemonData(command.getDaemonData());
        } catch (ParsingException e) {
            callback.reject(e);
            return;
        }
        flowData = flowData == null ? createFlowData() : flowData;

        RequestContext<FlowDataT, DaemonDataT, ResourcesT> context =
                new RequestContext<>(
                        this,
                        command.getFlow(),
                        command.getState(),
                        flowData,
                        daemonData,
                        resources,
                        callback);

        IHandler<Command, FlowDataT, DaemonDataT, ResourcesT> handler =
                incomingHandlers.get(command.getType());

        try{
            handler.handle(command, context);
        } catch (Exception e) {
            callback.reject(e);
        }
    }

    private Map<Command.Type, IHandler<Command, FlowDataT, DaemonDataT, ResourcesT>>
            incomingHandlers = new HashMap<>();

    {
        incomingHandlers.put(Command.Type.OPEN, (command, context) ->
                onOpen(parseRq(command.getContentBody()), context));

        incomingHandlers.put(Command.Type.ANSWER, (command, context) ->
                getState(command.getState())
                        .handle(command.getContentBody(), context));

        incomingHandlers.put(Command.Type.RETURN, (command, context) ->
                getState(command.getState())
                        .handle(command.getContentBody(), context));
    }

    public void sendTransition(String name, RequestContext<FlowDataT, DaemonDataT, ResourcesT> context) {
        RequestContext<FlowDataT, DaemonDataT, ResourcesT> transContext = new RequestContext<>(
                this,
                context.getFlowName(),
                name,
                context.getFlowData(),
                context.getDaemonData(),
                context.getResources(),
                context.getCallback()
        );
        getState(name).onEnter(transContext);
    }

    private ArgumentT parseRq(String rqString) throws ParsingException {
        return flowContract.getParser().parse(rqString, flowContract.getArgumentClass());
    }

    private FlowDataT parseFlowData(String flowData) throws ParsingException {
        return flowDataParser.parse(flowData, flowDataClass);
    }

    private DaemonDataT parseDaemonData(String flowData) throws ParsingException {
        return flowDataParser.parse(flowData, daemonDataClass);
    }

    String serializeFlowData(Object o) throws SerializingException {
        return flowDataParser.serialize(o);
    }

    private void validate() {
    }

}