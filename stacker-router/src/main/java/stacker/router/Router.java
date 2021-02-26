package stacker.router;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stacker.common.Command;
import stacker.common.ICallback;

public class Router {
    private static Logger log = LoggerFactory.getLogger(Router.class);

    private ITransport transport;
    private ISessionStorage sessionStorage;

    private Map<String, String> flows = new HashMap<>();
    private Map<String, Map<String, String>> flowMapping = new HashMap<>();
    private String mainFlow;

    private Map<Command.Type, ICallback<RouterResponseResult>> responseHandlers = new HashMap<>();
    private Map<String, IRouterCallback> sessionLock = new HashMap<>();

    public Router(ITransport transport, ISessionStorage sessionStorage) {
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

    public void setMainFlow(String name) {
        assertNotNull("mainFlow should not be null", name);
        name = name.trim().toUpperCase();
        assertNotNull("Flow '" + name + "' not found", flows.get(name));
        mainFlow = name;
    }

    public void setFlowMapping(String caller, String name, String target) {
        if (caller == null || name == null || target == null) return;
        caller = caller.trim().toUpperCase();
        name = name.trim().toUpperCase();
        target = target.trim().toUpperCase();
        //caller and target should be registered flow names
        if (!flowMapping.containsKey(caller))
            flowMapping.put(caller, new HashMap<>());
        Map<String, String> mapping = flowMapping.get(caller);
        mapping.put(name, target);
    }

    public String getMapped(String caller, String name) {
        if (caller == null || name == null) return null;
        caller = caller.trim().toUpperCase();
        name = name.trim().toUpperCase();
        if (!flowMapping.containsKey(caller)) return null;
        return flowMapping.get(caller).get(name);
    }

    private String getAddress(String name) {
        return flows.get(name.trim().toUpperCase());
    }

    public void handleRequest(String sid, String body, IRouterCallback callback) {
        synchronized (sid.intern()) {
            if (sessionLock.get(sid) != null) {
                sessionLock.put(sid, callback);
                return;
            }
            sessionLock.put(sid, callback);
        }

        sessionStorage.find(sid, new OnSessionFound(sid, body));
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
            Command.Type type = Command.Type.ANSWER;
            if (sessionStack == null || sessionStack.empty()) {
                sessionStack = new SessionStack();
                SessionStackEntry entry = new SessionStackEntry();
                entry.setFlow(mainFlow);
                sessionStack.push(entry);
                type = Command.Type.OPEN;
            }
            SessionStackEntry entry = sessionStack.peek();
            Command command = new Command();
            command.setType(type);
            command.setFlow(entry.getFlow());
            command.setState(entry.getState());
            command.setFlowData(entry.getFlowData());
            command.setContentBody(body);

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
                ICallback<RouterResponseResult> handler = responseHandlers.get(result.getType());
                
                if (handler == null) {
                    //write log
                    sessionLock.remove(sid)
                            .reject(new Exception("responseHandler not found"));
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
        responseHandlers.put(Command.Type.QUESTION, new ICallback<RouterResponseResult>() {

            @Override
            public void success(RouterResponseResult responseResult) {
                String sid = responseResult.getSid();
                SessionStack sessionStack = responseResult.getSessionStack();
                Command command = responseResult.getResponse();

                SessionStackEntry entry = sessionStack.peek();

                entry.setState(command.getState());
                entry.setFlowData(command.getFlowData());
                sessionStack.setDaemonData(entry.getFlow(), command.getDaemonData());
                entry.setContentBody(command.getContentBody());
                
                sessionStorage.save(sid, sessionStack);
                IRouterCallback routerCallback = sessionLock.remove(sid);
                routerCallback.success(sid, command.getContentBody());
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
                sessionStack.setDaemonData(currentEntry.getFlow(), command.getDaemonData());

                SessionStackEntry newEntry = new SessionStackEntry();
                newEntry.setFlow(
                        getMapped(
                                currentEntry.getFlow(),
                                command.getFlow()
                        )
                );

                sessionStack.push(newEntry);
                sessionStorage.save(sid, sessionStack);

                Command newCommand = new Command();
                newCommand.setType(Command.Type.OPEN);
                newCommand.setFlow(newEntry.getFlow());
                newCommand.setContentBody(command.getContentBody());
                newCommand.setDaemonData(sessionStack.getDaemonData(newEntry.getFlow()));

                transport.sendRequest(getAddress(newEntry.getFlow()), newCommand,
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

                SessionStackEntry entry = sessionStack.pop();
                sessionStack.setDaemonData(entry.getFlow(), command.getDaemonData());

                SessionStackEntry currentEntry = sessionStack.peek();

                Command newCommand = new Command();
                newCommand.setType(Command.Type.RETURN);
                newCommand.setFlow(currentEntry.getFlow());
                newCommand.setState(currentEntry.getState());
                newCommand.setFlowData(currentEntry.getFlowData());
                newCommand.setDaemonData(sessionStack.getDaemonData(currentEntry.getFlow()));
                newCommand.setContentBody(command.getContentBody());

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

    public interface IRouterCallback {
        void success(String sid, String body);

        void reject(Exception exception);
    }

}