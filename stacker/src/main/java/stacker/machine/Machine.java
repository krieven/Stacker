package stacker.machine;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import stacker.Command;
import stacker.ICallback;
import stacker.RouterCommand;

public class Machine<RqT, RsT, SessionT, ResourcesT> {
    private static ObjectMapper parser = new ObjectMapper();

    private Class<RqT> rqClass;
    private Class<RsT> rsClass;

    private Class<SessionT> sessionClass;
    private Class<ResourcesT> resourcesClass;

    private Map<String, State<Object, RsT, SessionT, ResourcesT>> states = new HashMap<>();

    public Machine(Class<RqT> rqClass, Class<RsT> rsClass, Class<SessionT> sessionClass, Class<ResourcesT> resourcesClass){
        this.sessionClass = sessionClass;
        this.resourcesClass = resourcesClass;
        this.rqClass = rqClass;
        this.rsClass = rsClass;
    }

    private RqT parseRq(String rqString) throws IOException{
        return parser.readValue(rqString, rqClass);
    }

    public void handle(Command command, ICallback<Command> callback){
        
    }

    private interface IncomingHandler{
        void handle(Command request, ICallback<Command> callback) throws Exception;
    }
    
    private Map<RouterCommand, IncomingHandler> incomingHandlers = new HashMap<>();
    {
        incomingHandlers.put(RouterCommand.ACTION, new IncomingHandler(){

            @Override
            public void handle(Command request, ICallback<Command> callback) throws Exception {

            }
        
        });

        incomingHandlers.put(RouterCommand.OPEN, new IncomingHandler(){

            @Override
            public void handle(Command request, ICallback<Command> callback) throws Exception {

            }
        
        });

        incomingHandlers.put(RouterCommand.RETURN, new IncomingHandler(){

            @Override
            public void handle(Command request, ICallback<Command> callback) throws Exception {

            }
        
        });
    }



}