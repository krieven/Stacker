package stacker.router;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stacker.Command;
import stacker.ICallback;

public class Router {
    private ICommandTransport transport;
    private ISessionStorage sessionStorage;
    private Map<String, String> flows = new HashMap<>();
    private String defaultFlow;
    private Map<Command.Type, ICallback<RouterResponseResult>> responseHandlers = new HashMap<>();
    private Map<String, IRouterCallback> sessionLock = new HashMap<>();

    private static Logger log = LoggerFactory.getLogger(Router.class);

    public Router(ICommandTransport transport, ISessionStorage sessionStorage) {
        assertNotNull(transport);
        assertNotNull(sessionStorage);
        this.transport = transport;
        this.sessionStorage = sessionStorage;
    }

    public void addFlow(String name, String address) {
        assertNotNull("Name should be not null", name);
        name = name.trim().toUpperCase();
        assertNotEquals("Name should not be empty string", name, "");
        assertNotNull("Address should be not null", address);
        assertNull("Flow '" + name + "' already registered", flows.get(name));
        flows.put(name, address);
    }

    public void setDefaultFlow(String name) {
        assertNull("defaultFlow already defined", defaultFlow);
        assertNotNull("defaultFlow should not be null", name);
        name = name.trim().toUpperCase();
        assertNotNull("Flow '" + name + "' not found", flows.get(name));
        defaultFlow = name;
    }

    private String getAddress(String name) {
        return flows.get(name.trim().toUpperCase());
    }

    public void validate() {
        assertNotEquals("At least one flow should be defined", flows.keySet().size(), 0);
        assertNotNull("Default flow should be defined", defaultFlow);
        assertNotNull("Default flow shold be one of defined flows", flows.get(defaultFlow));
    }

    public interface IRouterCallback {
        void success(String sid, String body);
        void reject(Exception exception);
    }

    private class OnSessionFound implements ICallback<SessionStack> {

        private String sid;
        private String body;

        OnSessionFound(String sid, String body) {
            this.sid = sid;
            this.body = body;
        }

        @Override
        public void success(SessionStack sessionStack) {
            Command.Type type = Command.Type.ACTION;
            if (sessionStack == null) {
                sessionStack = new SessionStack();
                SessionStackEntry entry = new SessionStackEntry();
                entry.setFlow(defaultFlow);
                sessionStack.push(entry);
                type = Command.Type.OPEN;
            }
            SessionStackEntry entry = sessionStack.peek();
            Command command = new Command();
            command.setCommand(type);
            command.setFlow(entry.getFlow());
            command.setState(entry.getState());
            command.setFlowData(entry.getFlowData());
            command.setBody(body);

            transport.sendRequest(getAddress(command.getFlow()), command, new OnResponseReceived(sid, sessionStack));
        }

        @Override
        public void reject(Exception exception) {
            IRouterCallback callback = sessionLock.remove(sid);
            if (callback != null)
                callback.reject(exception);
        }

    }

    private class OnResponseReceived implements ICallback<Command> {
        private String sid;
        private SessionStack sessionStack;

        OnResponseReceived(String sid, SessionStack sessionStack) {
            this.sid = sid;
            this.sessionStack = sessionStack;
        }

        @Override
        public void success(Command result) {
            synchronized (sid.intern()) {
                IRouterCallback routerCallback = sessionLock.get(sid);
                if(routerCallback == null){
                    //write log
                    return;
                }
                ICallback<RouterResponseResult> handler = responseHandlers.get(result.getCommand());
                
                if (handler == null) {
                    //write log
                    sessionLock.remove(sid).reject(new Exception("responseHandler not found"));
                    return;
                }
                RouterResponseResult responseResult = new RouterResponseResult(sid, sessionStack, result);
                handler.success(responseResult);
            }
        }

        @Override
        public void reject(Exception exception) {
            synchronized(sid.intern()){
                IRouterCallback callback = sessionLock.remove(sid);
                if (callback != null)
                    callback.reject(exception);
            }
        }

    }
    //here responseHandlers are
    {
        responseHandlers.put(Command.Type.RESULT, new ICallback<RouterResponseResult>() {

            @Override
            public void success(RouterResponseResult responseResult) {
                String sid = responseResult.getSid();
                SessionStack sessionStack = responseResult.getSessionStack();
                Command command = responseResult.getResponse();

                SessionStackEntry entry = sessionStack.peek();

                entry.setState(command.getState());
                entry.setFlowData(command.getFlowData());
                entry.setBody(command.getBody());
                
                sessionStorage.save(sid, sessionStack);
                IRouterCallback routerCallback = sessionLock.remove(sid);
                routerCallback.success(sid, command.getBody());
            }

            @Override
            public void reject(Exception error) {
                //
            }
        });

        responseHandlers.put(Command.Type.OPEN, new ICallback<RouterResponseResult>() {

            @Override
            public void success(RouterResponseResult responseResult) {
                String sid = responseResult.getSid();
                SessionStack sessionStack = responseResult.getSessionStack();                   
                Command command = responseResult.getResponse();

                SessionStackEntry currentEntry = sessionStack.peek();
                currentEntry.setState(command.getState());
                currentEntry.setFlowData(command.getFlowData());

                SessionStackEntry newEntry = new SessionStackEntry();
                newEntry.setFlow(command.getFlow());
                newEntry.setOnReturn(command.getOnReturn());

                sessionStack.push(newEntry);
                sessionStorage.save(sid, sessionStack);

                Command newCommand = new Command();
                newCommand.setCommand(Command.Type.OPEN);
                newCommand.setFlow(command.getFlow());
                newCommand.setBody(command.getBody());

                transport.sendRequest(getAddress(newCommand.getFlow()), newCommand,
                    new OnResponseReceived(sid, sessionStack)
                );
            }

            @Override
            public void reject(Exception error) {
                //
            }
        });

        responseHandlers.put(Command.Type.RETURN, new ICallback<RouterResponseResult>() {

            @Override
            public void success(RouterResponseResult responseResult) {
                String sid = responseResult.getSid();
                SessionStack sessionStack = responseResult.getSessionStack();                   
                Command command = responseResult.getResponse();

                SessionStackEntry popEntry = sessionStack.pop();
                SessionStackEntry currentEntry = sessionStack.peek();

                Command newCommand = new Command();
                newCommand.setCommand(Command.Type.RETURN);
                newCommand.setFlow(currentEntry.getFlow());
                newCommand.setState(currentEntry.getState());
                newCommand.setFlowData(currentEntry.getFlowData());
                newCommand.setOnReturn(popEntry.getOnReturn());
                newCommand.setBody(command.getBody());

                sessionStorage.save(sid, sessionStack);
                transport.sendRequest(getAddress(newCommand.getFlow()), newCommand,
                    new OnResponseReceived(sid, sessionStack)
                );
            }

            @Override
            public void reject(Exception error) {
                //
            }
        });

        responseHandlers.put(Command.Type.ERROR, new ICallback<RouterResponseResult>() {
            @Override
            public void success(RouterResponseResult routerResponseResult) {

            }

            @Override
            public void reject(Exception error) {
                //
            }
        });
    }

    public void handle(String sid, String body, IRouterCallback callback) {
        synchronized (sid.intern()) {
            if (sessionLock.get(sid) != null) {
                sessionLock.put(sid, callback);
                return;
            }
            sessionLock.put(sid, callback);
        }

        sessionStorage.find(sid, new OnSessionFound(sid, body));
    }

}