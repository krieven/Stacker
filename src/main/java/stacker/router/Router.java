package stacker.router;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import stacker.Command;
import stacker.ICallback;

public class Router {
    private ICommandTransport transport;
    private ISessionStorage sessionStorage;
    private Map<String, String> services = new HashMap<>();
    private String defaultService;
    private Map<Command.Type, ICallback<RouterResponseResult>> responseHandlers = new HashMap<>();
    private Map<String, IRouterCallback> sessionLock = new HashMap<>();

    public Router(ICommandTransport transport, ISessionStorage sessionStorage) {
        assertNotNull(transport);
        assertNotNull(sessionStorage);
        this.transport = transport;
        this.sessionStorage = sessionStorage;
    }

    public void addService(String name, String address) {
        assertNotNull("Name should be not null", name);
        assertNotNull("Address should be not null", address);
        assertNull("Service '" + name + "' already registered", services.get(name));
        services.put(name.trim(), address);
    }

    public void setDefaultService(String name) {
        assertNull("defaultService already defined", defaultService);
        assertNotNull("Service '" + name + "' not found", services.get(name));
        defaultService = name;
    }

    private String getAddress(String name) {
        return services.get(name);
    }

    public void validate() {
        assertNotEquals("At least one service should be defined", services.keySet().size(), 0);
        assertNotNull("Default service should be defined", defaultService);
        assertNotNull("Default service shold be one of defined services", services.get(defaultService));
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
                entry.setService(defaultService);
                sessionStack.push(entry);
                type = Command.Type.OPEN;
            }
            SessionStackEntry entry = sessionStack.getCurrent();
            Command command = new Command();
            command.setCommand(type);
            command.setService(entry.getService());
            command.setState(entry.getState());
            command.setStateData(entry.getStateData());
            command.setBody(body);

            transport.send(getAddress(command.getService()), command, new OnResponseReceived(sid, sessionStack));
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
            assertNotNull(sid);
            assertNotNull(sessionLock);
            assertNotNull(result);
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

                SessionStackEntry entry = sessionStack.getCurrent();

                entry.setState(command.getState());
                entry.setStateData(command.getStateData());
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

                SessionStackEntry currentEntry = sessionStack.getCurrent();
                currentEntry.setState(command.getState());
                currentEntry.setStateData(command.getStateData());

                SessionStackEntry newEntry = new SessionStackEntry();
                newEntry.setService(command.getService());
                newEntry.setOnReturn(command.getOnReturn());

                sessionStack.push(newEntry);
                sessionStorage.save(sid, sessionStack);

                Command newCommand = new Command();
                newCommand.setCommand(Command.Type.OPEN);
                newCommand.setService(command.getService());
                newCommand.setBody(command.getBody());

                transport.send(getAddress(newCommand.getService()), newCommand, 
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
                SessionStackEntry currentEntry = sessionStack.getCurrent();

                Command newCommand = new Command();
                newCommand.setCommand(Command.Type.RETURN);
                newCommand.setService(currentEntry.getService());
                newCommand.setState(currentEntry.getState());
                newCommand.setStateData(currentEntry.getStateData());
                newCommand.setOnReturn(popEntry.getOnReturn());
                newCommand.setBody(command.getBody());

                sessionStorage.save(sid, sessionStack);
                transport.send(getAddress(newCommand.getService()), newCommand,
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