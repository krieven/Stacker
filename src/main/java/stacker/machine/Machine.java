package stacker.machine;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import stacker.Command;
import stacker.ICallback;
import stacker.RouterCommand;

import static org.junit.Assert.*;

public class Machine<RqT, RsT, StateDataT, ResourcesT> {
    private static ObjectMapper PARSER = new ObjectMapper();

    private Class<RqT> rqClass;
    private Class<RsT> rsClass;

    private Class<StateDataT> sessionClass;

    private ResourcesT resources;

    private Map<String, State<Object, RsT, StateDataT, ResourcesT>> states = new HashMap<>();
    private IHandler<RqT, RsT, StateDataT, ResourcesT> onOpen;

    public Machine(Class<RqT> rqClass, Class<RsT> rsClass, Class<StateDataT> sessionClass, ResourcesT resources){
        this.sessionClass = sessionClass;
        this.rqClass = rqClass;
        this.rsClass = rsClass;
        this.resources = resources;
        try {
            rsClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private RqT parseRq(String rqString) {
        try {
            return PARSER.readValue(rqString, rqClass);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private StateDataT parseStateData(String stateData) {
        try {
            return PARSER.readValue(stateData, sessionClass);
        } catch (IOException e) {
            try {
                return sessionClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e1) {
                e1.printStackTrace();
            }
        }
        return null;
    }

    public void setOnOpen(IHandler<RqT, RsT, StateDataT, ResourcesT> onOpen){
        assertNull("onOpen handler already defined", this.onOpen);
        assertNotNull("OnOpen handler should not be null", onOpen);
        this.onOpen = onOpen;
    }

    public void addState(String name, State<Object, RsT, StateDataT, ResourcesT> state){
        assertNotNull("the NAME should not be null", name);
        name = name.trim().toUpperCase();
        assertNotEquals("the NAME should not be empty string", name, "");
        assertFalse("State with name '" + name + "' already registered", states.containsKey(name));
        assertNotNull("State should not be null", state);

        states.put(name,state);
    }

    private State<Object, RsT, StateDataT, ResourcesT> getState(String name) throws Exception{
        return states.get(name.trim().toUpperCase());
    }

    public void handle(Command command, ICallback<Command> callback){
        @SuppressWarnings("unchecked")
        MachineContext<RsT, StateDataT, ResourcesT> context =
                new MachineContext(this,
                        parseStateData(command.getStateData()),
                        resources, callback);

        IncomingHandler<RsT, StateDataT, ResourcesT> handler =
                incomingHandlers.get(command.getCommand());

        try{
            handler.handle(command, context);
        } catch (Exception e) {
            callback.reject(e);
        }
    }

    private interface IncomingHandler<RsT, StateDataT, ResourcesT>{
        void handle(Command request, MachineContext<RsT, StateDataT, ResourcesT> context) throws Exception;
    }
    
    private Map<RouterCommand, IncomingHandler<RsT, StateDataT, ResourcesT>> incomingHandlers
            = new HashMap<>();
    {
        incomingHandlers.put(RouterCommand.OPEN, new IncomingHandler<RsT, StateDataT, ResourcesT>(){
            @Override
            public void handle(Command request, MachineContext<RsT, StateDataT, ResourcesT> context) throws Exception {
                RqT rq = parseRq(request.getBody());
                onOpen.handle(rq, context);
            }
        });

        incomingHandlers.put(RouterCommand.ACTION, new IncomingHandler<RsT, StateDataT, ResourcesT>(){
            @Override
            public void handle(Command request, MachineContext<RsT, StateDataT, ResourcesT> context) throws Exception {
                String stateName = request.getState();
                State<Object, RsT, StateDataT, ResourcesT> state = getState(stateName);
                state.handle(request.getBody(), context);
            }
        });

        incomingHandlers.put(RouterCommand.RETURN, new IncomingHandler<RsT, StateDataT, ResourcesT>(){
            @Override
            public void handle(Command request, MachineContext<RsT, StateDataT, ResourcesT> context) throws Exception {

            }
        });
    }



}