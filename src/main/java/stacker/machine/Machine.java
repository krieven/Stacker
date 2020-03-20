package stacker.machine;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import stacker.Command;
import stacker.ICallback;
import stacker.RouterCommand;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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

    public void setOnOpen(IHandler<RqT, RsT, StateDataT, ResourcesT> onOpen){
        assertNull("onOpen handler already defined", this.onOpen);
        assertNotNull("OnOpen handler should not be null", onOpen);
        this.onOpen = onOpen;
    }

    public void handle(Command command, ICallback<Command> callback){
        @SuppressWarnings("unchecked")
        MachineContext<RsT, StateDataT, ResourcesT> context =
                new MachineContext(this,
                        parseSession(command.getStateData()),
                        resources);

        IncomingHandler<RsT, StateDataT, ResourcesT> handler =
                incomingHandlers.get(command.getCommand());

        try{
            handler.handle(command, context, callback);
        } catch (Exception e) {
            callback.reject(e);
        }
    }

    private StateDataT parseSession(String stateData) {
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

    private interface IncomingHandler<RsT, StateDataT, ResourcesT>{
        void handle(Command request, MachineContext<RsT, StateDataT, ResourcesT> context, ICallback<Command> callback) throws Exception;
    }
    
    private Map<RouterCommand, IncomingHandler<RsT, StateDataT, ResourcesT>> incomingHandlers
            = new HashMap<>();
    {
        incomingHandlers.put(RouterCommand.OPEN,
                new IncomingHandler<RsT, StateDataT, ResourcesT>(){
            @Override
            public void handle(Command request, MachineContext<RsT, StateDataT, ResourcesT> context, ICallback<Command> callback) throws Exception {

                RqT rq = parseRq(request.getBody());

                onOpen.handle(rq, context);

            }
        });

        incomingHandlers.put(RouterCommand.ACTION, new IncomingHandler<RsT, StateDataT, ResourcesT>(){
            @Override
            public void handle(Command request, MachineContext<RsT, StateDataT, ResourcesT> context, ICallback<Command> callback) throws Exception {

            }
        });

        incomingHandlers.put(RouterCommand.RETURN, new IncomingHandler<RsT, StateDataT, ResourcesT>(){
            @Override
            public void handle(Command request, MachineContext<RsT, StateDataT, ResourcesT> context, ICallback<Command> callback) throws Exception {

            }
        });
    }



}