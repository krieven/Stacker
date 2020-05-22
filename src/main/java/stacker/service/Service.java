package stacker.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stacker.Command;
import stacker.ICallback;

import static org.junit.Assert.*;

public abstract class Service<OpenArgT, ReturnT, StateDataT, ResourcesT> {
    private static Logger log = LoggerFactory.getLogger(Service.class);
    private static ObjectMapper PARSER = new ObjectMapper();

    private Class<OpenArgT> openArgTClass;
    private Class<StateDataT> stateDataTClass;
    private ResourcesT resources;

    public Service(Class<OpenArgT> openArgTClass, Class<StateDataT> stateDataTClass, ResourcesT resources) {
        this.stateDataTClass = stateDataTClass;
        this.openArgTClass = openArgTClass;
        this.resources = resources;
        this.configure();
        this.validate();
    }

    public abstract void configure();

    public abstract StateDataT createStateData();

    public abstract ReturnT makeReturn(RequestContext<StateDataT, ResourcesT> context);


    private Map<String, State<?, ?, StateDataT, ResourcesT>> states = new HashMap<>();
    private IHandler<OpenArgT, StateDataT, ResourcesT> onOpenHandler;

    public final <A, R> void addState(String name, State<A, R, StateDataT, ResourcesT> state) {
        assertNotNull("The NAME should not be null", name);
        name = name.trim().toUpperCase();
        assertNotEquals("The NAME should not be empty string", name, "");
        assertFalse("State with name '" + name + "' already registered", states.containsKey(name));
        assertNotNull("State should not be null", state);

        states.put(name, state);
    }

    public final void setOnOpenHandler(IHandler<OpenArgT, StateDataT, ResourcesT> onOpenHandler) {
        assertNotNull("onOpenHandler should not be null", onOpenHandler);
        this.onOpenHandler = onOpenHandler;
    }

    private State<?, ?, StateDataT, ResourcesT> getState(String name) {
        return states.get(name.trim().toUpperCase());
    }

    private void handleOpen(OpenArgT arg, RequestContext<StateDataT, ResourcesT> context) {
        onOpenHandler.handle(arg, context);
    }

    /**
     * This method will be called by server to handle request
     *
     * @param command  - the Command that server receive
     * @param callback - the server callback
     */
    public final void handleCommand(Command command, ICallback<Command> callback) {

        StateDataT stateData = parseStateData(command.getStateData());
        stateData = stateData == null ? createStateData() : stateData;

        RequestContext<StateDataT, ResourcesT> context =
                new RequestContext<>(
                        this,
                        command.getService(),
                        command.getState(),
                        stateData,
                        resources,
                        callback);

        IHandler<Command, StateDataT, ResourcesT> handler =
                incomingHandlers.get(command.getCommand());

        try{
            handler.handle(command, context);
        } catch (Exception e) {
            callback.reject(e);
        }
    }

    private Map<Command.Type, IHandler<Command, StateDataT, ResourcesT>> incomingHandlers
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

    void sendTransition(String name, RequestContext<StateDataT, ResourcesT> context) {
        RequestContext<StateDataT, ResourcesT> transContext = new RequestContext<>(
                this,
                context.getServiceName(),
                name,
                context.getStateData(),
                context.getResources(),
                context.getCallback()
        );
        getState(name).handleOpen(transContext);
    }

    private OpenArgT parseRq(String rqString) {
        try {
            return PARSER.readValue(rqString, openArgTClass);
        } catch (IOException e) {
            log.error("Error parsing request", e);
            return null;
        }
    }

    private StateDataT parseStateData(String stateData) {
        try {
            return PARSER.readValue(stateData, stateDataTClass);
        } catch (IOException e) {
            log.error("Error parsing stateData", e);
        }
        return null;
    }

    private void validate() {
    }

}