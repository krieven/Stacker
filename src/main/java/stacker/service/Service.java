package stacker.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import stacker.Command;
import stacker.ICallback;

import static org.junit.Assert.*;

public abstract class Service<OpenArgT, ReturnT, StateDataT, ResourcesT> {
    private static ObjectMapper PARSER = new ObjectMapper();

    private Class<OpenArgT> openArgTClass;
    private Class<StateDataT> stateDataTClass;
    private ResourcesT resources;

    public Service(Class<OpenArgT> openArgTClass, Class<StateDataT> stateDataTClass, ResourcesT resources) {
        this.stateDataTClass = stateDataTClass;
        this.openArgTClass = openArgTClass;
        this.resources = resources;
        this.init();
        this.validate();
    }

    public abstract void init();

    public abstract StateDataT createStateData();

    public abstract void onOpen(OpenArgT argument, ServiceContext<StateDataT, ResourcesT> context);

    public abstract ReturnT makeReturn(ServiceContext<StateDataT, ResourcesT> context);


    private Map<String, State<?, ?, StateDataT, ResourcesT>> states = new HashMap<>();

    public final <A, R> void addState(String name, State<A, R, StateDataT, ResourcesT> state) {
        assertNotNull("The NAME should not be null", name);
        name = name.trim().toUpperCase();
        assertNotEquals("The NAME should not be empty string", name, "");
        assertFalse("State with name '" + name + "' already registered", states.containsKey(name));
        assertNotNull("State should not be null", state);

        states.put(name, state);
    }

    private State<?, ?, StateDataT, ResourcesT> getState(String name) {
        return states.get(name.trim().toUpperCase());
    }

    public final void handle(Command command, ICallback<Command> callback) {

        StateDataT stateData = parseStateData(command.getStateData());
        stateData = stateData == null ? createStateData() : stateData;

        ServiceContext<StateDataT, ResourcesT> context =
                new ServiceContext<>(
                        this,
                        command.getService(),
                        command.getState(),
                        stateData,
                        resources,
                        callback);

        IncomingHandler<StateDataT, ResourcesT> handler =
                incomingHandlers.get(command.getCommand());

        try{
            handler.handle(command, context);
        } catch (Exception e) {
            callback.reject(e);
        }
    }

    private Map<Command.Type, IncomingHandler<StateDataT, ResourcesT>> incomingHandlers
            = new HashMap<>();

    {
        incomingHandlers.put(Command.Type.OPEN, (command, context) ->
                onOpen(parseRq(command.getBody()), context));

        incomingHandlers.put(Command.Type.ACTION, (command, context) -> getState(command.getState())
                .handleAction(command.getBody(), context));

        incomingHandlers.put(Command.Type.RETURN, (command, context) -> getState(command.getState())
                .handleReturn(command.getOnReturn(), command.getBody(), context));
    }

    void sendTransition(String name, ServiceContext<StateDataT, ResourcesT> context) {
        ServiceContext<StateDataT, ResourcesT> transContext = new ServiceContext<StateDataT, ResourcesT>(
                this,
                context.getServiceName(),
                name,
                context.getStateData(),
                context.getResources(),
                context.getCallback()
        );
        getState(name).handleInit(transContext);
    }

    private OpenArgT parseRq(String rqString) {
        try {
            return PARSER.readValue(rqString, openArgTClass);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private StateDataT parseStateData(String stateData) {
        try {
            return PARSER.readValue(stateData, stateDataTClass);
        } catch (IOException e) {
            try {
                return stateDataTClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e1) {
                e1.printStackTrace();
            }
        }
        return null;
    }

    private void validate() {
    }

    private interface IncomingHandler<StateDataT, ResourcesT> {
        void handle(Command command, ServiceContext<StateDataT, ResourcesT> context);
    }

}