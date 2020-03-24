package stacker.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import stacker.Command;
import stacker.ICallback;

import static org.junit.Assert.*;

public class Service<OpenArgT, ReturnT, StateDataT, ResourcesT> {
    private static ObjectMapper PARSER = new ObjectMapper();

    private Class<OpenArgT> openArgTClass;

    private Class<StateDataT> stateDataTClass;

    private ResourcesT resources;

    private Map<String, State<?, ?, ReturnT, StateDataT, ResourcesT>> states = new HashMap<>();
    private IHandler<OpenArgT, StateDataT, ResourcesT> onOpen;

    public Service(Class<OpenArgT> openArgTClass, Class<StateDataT> stateDataTClass, ResourcesT resources) {
        this.stateDataTClass = stateDataTClass;
        this.openArgTClass = openArgTClass;
        this.resources = resources;
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

    public void setOnOpen(IHandler<OpenArgT, StateDataT, ResourcesT> onOpen) {
        assertNull("onOpen handler already defined", this.onOpen);
        assertNotNull("onOpen handler should not be null", onOpen);
        this.onOpen = onOpen;
    }

    public <A, R> State<A, R, ReturnT, StateDataT, ResourcesT>
    addState(String name, State<A, R, ReturnT, StateDataT, ResourcesT> state) {
        assertNotNull("The NAME should not be null", name);
        name = name.trim().toUpperCase();
        assertNotEquals("The NAME should not be empty string", name, "");
        assertFalse("State with name '" + name + "' already registered", states.containsKey(name));
        assertNotNull("State should not be null", state);

        states.put(name, state);
        state.setService(this);
        return state;
    }

    private State<?, ?, ReturnT, StateDataT, ResourcesT> getState(String name) {
        return states.get(name.trim().toUpperCase());
    }

    public void handle(Command command, ICallback<Command> callback){
        @SuppressWarnings("unchecked")
        ServiceContext<StateDataT, ResourcesT> context =
                new ServiceContext(
                        command.getService(),
                        command.getState(),
                        parseStateData(command.getStateData()),
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

    private interface IncomingHandler<StateDataT, ResourcesT> {
        void handle(Command command, ServiceContext<StateDataT, ResourcesT> context) throws Exception;
    }

    private Map<Command.Type, IncomingHandler<StateDataT, ResourcesT>> incomingHandlers
            = new HashMap<>();
    {
        incomingHandlers.put(Command.Type.OPEN, new IncomingHandler<StateDataT, ResourcesT>() {
            @Override
            public void handle(Command command, ServiceContext<StateDataT, ResourcesT> context) throws Exception {
                OpenArgT rq = parseRq(command.getBody());
                onOpen.handle(rq, context);
            }
        });

        incomingHandlers.put(Command.Type.ACTION, new IncomingHandler<StateDataT, ResourcesT>() {
            @Override
            public void handle(Command command, ServiceContext<StateDataT, ResourcesT> context) throws Exception {
                String stateName = command.getState();
                State<?, ?, ReturnT, StateDataT, ResourcesT> state = getState(stateName);
                state.handleAction(command.getBody(), context);
            }
        });

        incomingHandlers.put(Command.Type.RETURN, new IncomingHandler<StateDataT, ResourcesT>() {
            @Override
            public void handle(Command command, ServiceContext<StateDataT, ResourcesT> context) throws Exception {

            }
        });
    }

    public void sendTransition(String name, ServiceContext<StateDataT, ResourcesT> context) {
        this.getState(name).open();
    }

}