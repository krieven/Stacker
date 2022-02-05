package io.github.krieven.stacker.router;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.*;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.krieven.stacker.common.config.router.FlowConfig;
import io.github.krieven.stacker.common.config.router.NameMapping;
import io.github.krieven.stacker.common.config.router.RouterConfig;
import io.github.krieven.stacker.common.dto.Command;
import io.github.krieven.stacker.common.ICallback;
import io.github.krieven.stacker.common.dto.ResourceRequest;

public class Router {
    private static final Logger log = LoggerFactory.getLogger(Router.class);

    private final ITransport transport;
    private final ISessionStorage sessionStorage;

    private final Map<String, String> flows = new HashMap<>();
    private final Map<String, Map<String, String>> flowMapping = new HashMap<>();
    private Map<String, Map<String, String>> properties = new HashMap<>();
    private String mainFlow;

    private final Map<Command.Type, Consumer<RouterResponseResult>> responseHandlers = new HashMap<>();
    private final Map<String, IRouterCallback> sessionLock = new HashMap<>();

    //here responseHandlers are
    {
        responseHandlers.put(Command.Type.QUESTION, new Consumer<RouterResponseResult>() {

            @Override
            public void accept(RouterResponseResult responseResult) {
                String sid = responseResult.getSid();
                SessionStack sessionStack = responseResult.getSessionStack();
                Command command = responseResult.getResponse();

                SessionStackEntry entry = sessionStack.peek();

                entry.setState(command.getState());
                entry.setFlowData(command.getFlowData());

                sessionStorage.save(sid, sessionStack);
                IRouterCallback routerCallback = sessionLock.remove(sid);
                routerCallback.success(sid, command.getBodyContentType(), command.getContentBody());
            }
        });

        responseHandlers.put(Command.Type.OPEN, new Consumer<RouterResponseResult>() {

            @Override
            public void accept(RouterResponseResult responseResult) {
                String sid = responseResult.getSid();
                SessionStack sessionStack = responseResult.getSessionStack();
                Command command = responseResult.getResponse();

                SessionStackEntry currentEntry = sessionStack.peek();

                currentEntry.setState(command.getState());
                currentEntry.setFlowData(command.getFlowData());

                SessionStackEntry newEntry = new SessionStackEntry();
                newEntry.setFlow(
                        getMapped(
                                currentEntry.getFlow(),
                                command.getFlow()
                        )
                );
                newEntry.setAddress(getAddress(newEntry.getFlow()));
                sessionStack.push(newEntry);
                sessionStorage.save(sid, sessionStack);

                Command newCommand = new Command();
                newCommand.setType(Command.Type.OPEN);
                newCommand.setFlow(newEntry.getFlow());
                newCommand.setContentBody(command.getContentBody());
                newCommand.setFlowData(sessionStack.getDaemonData(newEntry.getFlow()));

                try {
                    transport.sendRequest(newEntry.getAddress(), newCommand,
                            new ResponseCallback(sid, sessionStack)
                    );
                } catch (Exception e) {
                    sessionLock.remove(sid).reject(e);
                }
            }
        });

        responseHandlers.put(Command.Type.RETURN, new Consumer<RouterResponseResult>() {

            @Override
            public void accept(RouterResponseResult responseResult) {
                String sid = responseResult.getSid();
                SessionStack sessionStack = responseResult.getSessionStack();
                Command command = responseResult.getResponse();

                String flow = sessionStack.pop().getFlow();
                sessionStack.setDaemonData(flow, command.getFlowData());
                sessionStorage.save(sid, sessionStack);

                if (!sessionStack.empty()) {
                    SessionStackEntry currentEntry = sessionStack.peek();

                    Command newCommand = new Command();
                    newCommand.setType(Command.Type.RETURN);
                    newCommand.setFlow(currentEntry.getFlow());
                    newCommand.setState(currentEntry.getState());
                    newCommand.setFlowData(currentEntry.getFlowData());
                    newCommand.setContentBody(command.getContentBody());

                    try {
                        transport.sendRequest(currentEntry.getAddress(), newCommand,
                                new ResponseCallback(sid, sessionStack)
                        );
                    } catch (Exception e) {
                        new ResponseCallback(sid, sessionStack).reject(e);
                    }
                    return;
                }
                sessionLock.remove(sid).success(sid, command.getBodyContentType(), command.getContentBody());
            }
        });

        responseHandlers.put(Command.Type.ERROR, result -> {
            String sid = result.getSid();
            IRouterCallback routerCallback = sessionLock.remove(sid);
            Command command = result.getResponse();
            routerCallback.success(sid, command.getBodyContentType(), command.getContentBody());
        });

        responseHandlers.put(Command.Type.RESOURCE, responseHandlers.get(Command.Type.ERROR));

        responseHandlers.put(Command.Type.ANSWER, responseHandlers.get(Command.Type.ERROR));
    }

    public Router(@NotNull ITransport transport, @NotNull ISessionStorage sessionStorage) {
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

    public void setFlowMapping(@NotNull String caller, @NotNull String name, @NotNull String target) {
        caller = caller.trim().toUpperCase();
        name = name.trim().toUpperCase();
        target = target.trim().toUpperCase();
        //caller and target should be registered flow names
        if (!flowMapping.containsKey(caller)) {
            flowMapping.put(caller, new HashMap<>());
        }
        Map<String, String> mapping = flowMapping.get(caller);
        mapping.put(name, target);
    }

    public boolean setConfig(RouterConfig config) {
        if (config == null || config.getFlows() == null) {
            return false;
        }
        for (FlowConfig flowConfig : config.getFlows()) {
            addFlow(flowConfig.getName(), flowConfig.getAddress());
            setProperties(flowConfig.getName(), flowConfig.getProperties());

            if (flowConfig.getMapping() == null) {
                continue;
            }
            for (NameMapping mapping : flowConfig.getMapping()) {
                setFlowMapping(flowConfig.getName(), mapping.getName(), mapping.getTarget());
            }
        }
        setMainFlow(config.getMainFlow());

        return isValidConfiguration();
    }

    private void setProperties(String name, Map<String, String> properties) {
        this.properties.put(name, properties);
    }

    public boolean isValidConfiguration() {
        boolean result = true;
        if (flows.isEmpty()) {
            log.error("no flows configured");
            result = false;
        }
        if (mainFlow == null || !flows.containsKey(mainFlow)) {
            log.error("mainFlow should be configured flow name");
            result = false;
        }

        result = result && !hasRecursion(new ArrayList<>(), mainFlow);

        return result;
    }

    private boolean hasRecursion(List<String> path, String name) {
        if (path.contains(name)) {
            log.error(name + " - is recursively mapped in {}", path);
            return true;
        }
        Map<String, String> mapping = flowMapping.get(name);
        if (mapping == null) {
            return false;
        }

        List<String> newPath = new ArrayList<>(path);
        newPath.add(name);

        boolean result = false;

        for (String child : mapping.values()) {
            result = result || hasRecursion(newPath, child);
        }
        return result;
    }

    private String getMapped(@NotNull String caller, @NotNull String name) {

        caller = caller.trim().toUpperCase();
        name = name.trim().toUpperCase();
        if (!flowMapping.containsKey(caller)) {
            throw new IllegalStateException("no flows mapped on '" + name + "' for '" + caller + "'");
        }
        return flowMapping.get(caller).get(name);
    }

    private String getAddress(String name) {
        if (name == null) {
            log.info("Flow cannot be found, null name detected");
            throw new IllegalArgumentException("Flow cannot be found, null name detected");
        }
        name = name.trim().toUpperCase();
        if (!flows.containsKey(name)) {
            log.info("Flow cannot be found");
            throw new IllegalArgumentException("Flow cannot be found, name \"" + name + "\" not configured");
        }
        return flows.get(name);
    }

    private boolean putSessionLock(String sid, IRouterCallback callback) {
        synchronized (sid.intern()) {
            if (sessionLock.get(sid) != null) {
                sessionLock.put(sid, callback);
                return false;
            }
            sessionLock.put(sid, callback);
            return true;
        }
    }

    public void handleRequest(String sid, byte[] body, IRouterCallback callback) {
        if (putSessionLock(sid, callback)) {
            sessionStorage.find(sid, new SessionCallback(sid, body));
        }
    }

    public void handleResourceRequest(String sid, String path, Map<String, String[]> parameters, IRouterCallback callback) {
        ResourceRequest resourceRequest = new ResourceRequest();
        resourceRequest.setPath(path);
        resourceRequest.setParameters(parameters);
        sessionStorage.find(sid, new SessionResourceCallback(sid, resourceRequest, callback));
    }

    public interface IRouterCallback {
        void success(String sid, String contentType, byte[] body);

        void reject(Exception exception);
    }

    private class SessionCallback implements ICallback<SessionStack> {

        private final String sid;
        private final byte[] body;

        SessionCallback(String sid, byte[] body) {
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
                entry.setAddress(getAddress(mainFlow));
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
            command.setProperties(properties.get(entry.getFlow()));

            try {
                transport.sendRequest(entry.getAddress(), command, new ResponseCallback(sid, sessionStack));
            } catch (Exception e) {
                reject(e);
            }
        }

        @Override
        public void reject(Exception exception) {
            synchronized (sid.intern()) {
                log.error("SessionStorage rejects with exception: " + exception.getMessage(), exception);
                IRouterCallback callback = sessionLock.remove(sid);
                if (callback != null) {
                    callback.reject(exception);
                }
            }
        }
    }

    private class SessionResourceCallback implements ICallback<SessionStack> {

        private final String sid;
        private final ResourceRequest resourceRequest;
        private final IRouterCallback callback;

        SessionResourceCallback(String sid, ResourceRequest resourceRequest, IRouterCallback callback) {
            this.sid = sid;
            this.resourceRequest = resourceRequest;
            this.callback = callback;
        }

        @Override
        public void success(SessionStack stack) {

            if (stack == null || stack.empty()) {
                reject(new Exception("Session stack not found or empty"));
                return;
            }

            SessionStackEntry entry = stack.peek();

            Command command = new Command();
            command.setType(Command.Type.RESOURCE);
            command.setFlow(entry.getFlow());
            command.setState(entry.getState());
            command.setFlowData(entry.getFlowData());
            command.setResourceRequest(resourceRequest);
            command.setProperties(properties.get(entry.getFlow()));

            transport.sendRequest(entry.getAddress(), command, new ICallback<Command>() {
                @Override
                public void success(Command result) {
                    if (result == null) {
                        callback.reject(new Exception("Flow " + entry.getFlow() + " returns empty response"));
                        return;
                    }
                    callback.success(sid, result.getBodyContentType(), result.getContentBody());
                }

                @Override
                public void reject(Exception error) {
                    callback.reject(new Exception("Flow " + entry.getFlow() + " is broken cause " + error.getMessage(), error));
                }
            });
        }

        @Override
        public void reject(Exception error) {
            Exception exception = new Exception("Resource request have been rejected by router, SessionStorage rejects: " + error.getMessage(), error);
            log.error("bad response from session storage", exception);
            callback.reject(exception);
        }
    }

    private class ResponseCallback implements ICallback<Command> {
        private final String sid;
        private final SessionStack sessionStack;

        ResponseCallback(String sid, SessionStack sessionStack) {
            this.sid = sid;
            this.sessionStack = sessionStack;
        }

        @Override
        public void success(Command result) {
            synchronized (sid.intern()) {
                IRouterCallback routerCallback = sessionLock.get(sid);
                if (routerCallback == null) {
                    log.error("router callback was not found for sid=" + sid);
                    return;
                }
                if (result == null) {
                    routerCallback.reject(new Exception("null response from flow"));
                    return;
                }
                Consumer<RouterResponseResult> handler = responseHandlers.get(result.getType());

                if (handler == null) {
                    log.error("Unknown response type, handler not found for " + result.getType());
                    sessionLock.remove(sid)
                            .reject(new Exception("Unknown response type, responseHandler not found"));
                    return;
                }
                RouterResponseResult responseResult = new RouterResponseResult(sid, sessionStack, result);
                handler.accept(responseResult);
            }
        }

        @Override
        public void reject(Exception exception) {
            log.error("Transport rejects with exception " + exception.getMessage(), exception);
            synchronized (sid.intern()) {
                IRouterCallback callback = sessionLock.remove(sid);
                if (callback != null) {
                    callback.reject(exception);
                }
            }
        }

    }

}