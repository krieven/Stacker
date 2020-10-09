package stacker.flow;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stacker.Command;
import stacker.ICallback;

import static org.junit.Assert.*;

public abstract class Flow<OpenArgT, ReturnT, FlowDataT, ResourcesT> {
    private static Logger log = LoggerFactory.getLogger(Flow.class);
    private static ObjectMapper PARSER = new ObjectMapper();

    private Class<OpenArgT> openArgTClass;
    private Class<ReturnT> returnTClass;
    private Class<FlowDataT> flowDataTClass;
    private ResourcesT resources;

    public Flow(Class<OpenArgT> openArgTClass, Class<ReturnT> returnTClass, Class<FlowDataT> flowDataTClass, ResourcesT resources) {
        this.flowDataTClass = flowDataTClass;
        this.openArgTClass = openArgTClass;
        this.returnTClass = returnTClass;
        this.resources = resources;
        this.configure();
        this.validate();
    }

    public abstract void configure();

    public abstract FlowDataT createFlowData();

    public abstract ReturnT makeReturn(RequestContext<FlowDataT, ResourcesT> context);


    private Map<String, State<?, ?, FlowDataT, ResourcesT>> states = new HashMap<>();
    private IHandler<OpenArgT, FlowDataT, ResourcesT> onOpenHandler;

    public final FlowContract getContract() {
        return new FlowContract(openArgTClass, returnTClass);
    }

    public final <A, R> void addState(String name, State<A, R, FlowDataT, ResourcesT> state) {
        assertNotNull("The NAME should not be null", name);
        name = name.trim().toUpperCase();
        assertNotEquals("The NAME should not be empty string", name, "");
        assertFalse("ActionState with name '" + name + "' already registered", states.containsKey(name));
        assertNotNull("ActionState should not be null", state);

        states.put(name, state);
    }

    public final void setOnOpenHandler(IHandler<OpenArgT, FlowDataT, ResourcesT> onOpenHandler) {
        assertNotNull("onOpenHandler should not be null", onOpenHandler);
        this.onOpenHandler = onOpenHandler;
    }

    private State<?, ?, FlowDataT, ResourcesT> getState(String name) {
        return states.get(name.trim().toUpperCase());
    }

    private void handleOpen(OpenArgT arg, RequestContext<FlowDataT, ResourcesT> context) {
        onOpenHandler.handle(arg, context);
    }

    /**
     * This method will be called by server to handle request
     *
     * @param command  - the Command that server receive
     * @param callback - the server callback
     */
    public final void handleCommand(Command command, ICallback<Command> callback) {

        FlowDataT flowData = parseFlowData(command.getFlowData());
        flowData = flowData == null ? createFlowData() : flowData;

        RequestContext<FlowDataT, ResourcesT> context =
                new RequestContext<>(
                        this,
                        command.getFlow(),
                        command.getState(),
                        flowData,
                        resources,
                        callback);

        IHandler<Command, FlowDataT, ResourcesT> handler =
                incomingHandlers.get(command.getCommand());

        try{
            handler.handle(command, context);
        } catch (Exception e) {
            callback.reject(e);
        }
    }

    private Map<Command.Type, IHandler<Command, FlowDataT, ResourcesT>> incomingHandlers
            = new HashMap<>();

    {
        incomingHandlers.put(Command.Type.OPEN, (command, context) ->
                handleOpen(parseRq(command.getBody()), context));

        incomingHandlers.put(Command.Type.ACTION, (command, context) ->
                getState(command.getState())
                .handleAction(command.getBody(), context));

        incomingHandlers.put(Command.Type.RETURN, (command, context) ->
                getState(command.getState())
                .handleReturn(command.getOnReturn(), command.getBody(), context));
    }

    void sendTransition(String name, RequestContext<FlowDataT, ResourcesT> context) {
        RequestContext<FlowDataT, ResourcesT> transContext = new RequestContext<>(
                this,
                context.getFlowName(),
                name,
                context.getFlowData(),
                context.getResources(),
                context.getCallback()
        );
        getState(name).onOpen(transContext);
    }

    private OpenArgT parseRq(String rqString) {
        try {
            return PARSER.readValue(rqString, openArgTClass);
        } catch (IOException e) {
            log.error("Error parsing request", e);
            return null;
        }
    }

    private FlowDataT parseFlowData(String flowData) {
        try {
            return PARSER.readValue(flowData, flowDataTClass);
        } catch (IOException e) {
            log.error("Error parsing flowData", e);
        }
        return null;
    }

    private void validate() {
    }

}