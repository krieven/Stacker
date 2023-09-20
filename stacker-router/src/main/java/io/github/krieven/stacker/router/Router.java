package io.github.krieven.stacker.router;

import static org.junit.Assert.assertNotNull;

import java.util.*;
import java.util.function.Consumer;

import io.github.krieven.stacker.common.config.router.RouterConfig;
import io.github.krieven.stacker.common.config.router.RouterConfigValidator;
import io.github.krieven.stacker.util.Probe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.krieven.stacker.common.dto.Command;
import io.github.krieven.stacker.common.ICallback;
import io.github.krieven.stacker.common.dto.ResourceRequest;

import javax.validation.constraints.NotNull;

public class Router {
    private static final Logger log = LoggerFactory.getLogger(Router.class);

    private final ITransport transport;
    private final ISessionStorage sessionStorage;

    private final Map<Command.Type, Consumer<RouterResponseResult>> responseHandlers = new HashMap<>();
    private final Map<String, IRouterCallback> sessionLock = new HashMap<>();
    private RouterConfig config;

    //here responseHandlers are
    {
        responseHandlers.put(Command.Type.QUESTION, new Consumer<>() {

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

        responseHandlers.put(Command.Type.OPEN, new Consumer<>() {

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
                        config.resolveSubFlow(
                                currentEntry.getFlow(),
                                command.getFlow()
                        )
                );
                newEntry.setAddress(config.resolveAddress(newEntry.getFlow()));
                sessionStack.push(newEntry);
                sessionStorage.save(sid, sessionStack);

                Command newCommand = new Command();
                newCommand.setType(Command.Type.OPEN);
                newCommand.setRqUid(command.getRqUid());
                newCommand.setFlow(newEntry.getFlow());
                newCommand.setContentBody(command.getContentBody());
                newCommand.setFlowData(sessionStack.getDaemonData(newEntry.getFlow()));

                try {
                    transport.sendRequest(newEntry.getAddress(), newCommand,
                            new ResponseCallback(sid, sessionStack)
                    );
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    sessionLock.remove(sid).reject(e);
                }
            }
        });

        responseHandlers.put(Command.Type.RETURN, new Consumer<>() {

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
                    newCommand.setRqUid(command.getRqUid());
                    newCommand.setFlow(currentEntry.getFlow());
                    newCommand.setState(currentEntry.getState());
                    newCommand.setFlowData(currentEntry.getFlowData());
                    newCommand.setContentBody(command.getContentBody());

                    try {
                        transport.sendRequest(currentEntry.getAddress(), newCommand,
                                new ResponseCallback(sid, sessionStack)
                        );
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
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

    public boolean setConfig(RouterConfig config) {
        if (!RouterConfigValidator.isValid(config)) {
            return false;
        }
        this.config = config;
        return true;
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
                String mainFlow = config.getMainFlow();

                sessionStack = new SessionStack();
                SessionStackEntry entry = new SessionStackEntry();

                entry.setFlow(mainFlow);
                entry.setAddress(config.resolveAddress(mainFlow));
                sessionStack.push(entry);
                type = Command.Type.OPEN;
            }
            SessionStackEntry entry = sessionStack.peek();
            Command command = new Command();
            command.setRqUid(UUID.randomUUID().toString());
            command.setType(type);
            command.setFlow(entry.getFlow());
            command.setState(entry.getState());
            command.setFlowData(entry.getFlowData());
            command.setContentBody(body);
            command.setProperties(config.resolveProperties(entry.getFlow()));

            log.info("sid:{}; rqUid:{}; flow:{}; state:{}; type:{}; send",
                    sid, command.getRqUid(), command.getFlow(), command.getState(), command.getType());
            try {
                transport.sendRequest(entry.getAddress(), command, new ResponseCallback(sid, sessionStack));
            } catch (Exception e) {
                log.error("sid:{}; rqUid:{}; flow:{}; state:{}; type:{}; rejected",
                        sid, command.getRqUid(), command.getFlow(), command.getState(), command.getType(), e);
                reject(e);
            }
        }

        @Override
        public void reject(Exception exception) {
            synchronized (sid.intern()) {
                log.error("SessionStorage {} rejects with exception: " + exception.getMessage(), sid, exception);
                IRouterCallback callback = sessionLock.remove(sid);
                if (callback != null) {
                    log.error("callback is not null");
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
            command.setRqUid(UUID.randomUUID().toString());
            command.setFlow(entry.getFlow());
            command.setState(entry.getState());
            command.setFlowData(entry.getFlowData());
            command.setResourceRequest(resourceRequest);
            command.setProperties(config.resolveProperties(entry.getFlow()));

            transport.sendRequest(entry.getAddress(), command, new ICallback<>() {
                @Override
                public void success(Command result) {
                    if (result == null) {
                        log.error("result is null");
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
        private final String flowName;

        ResponseCallback(String sid, SessionStack sessionStack) {
            this.sid = sid;
            this.sessionStack = sessionStack;
            this.flowName = Probe.tryGet(() -> sessionStack.peek().getFlow()).orElse("[unknown]");
        }

        @Override
        public void success(Command result) {
            synchronized (sid.intern()) {
                IRouterCallback routerCallback = sessionLock.get(sid);
                if (routerCallback == null) {
                    log.error("router callback was not found for sid [{}], rqUid [{}]", sid, result.getRqUid());
                    return;
                }
                if (result == null) {
                    log.error("null response from flow [{}] for sid [{}]", flowName, sid);
                    routerCallback.reject(new Exception("null response from flow"));
                    return;
                }
                Consumer<RouterResponseResult> handler = responseHandlers.get(result.getType());

                if (handler == null) {
                    log.error("Unknown response type, handler not found from flow [{}] for [{}]",
                            flowName, result.getType());
                    sessionLock.remove(sid)
                            .reject(new Exception("Unknown response type, responseHandler not found from " + flowName));
                    return;
                }
                RouterResponseResult responseResult = new RouterResponseResult(sid, sessionStack, result);
                handler.accept(responseResult);
            }
        }

        @Override
        public void reject(Exception exception) {
            log.error("Transport rejects with exception [{}] from flow [{}]", exception.getMessage(), flowName, exception);
            synchronized (sid.intern()) {
                IRouterCallback callback = sessionLock.remove(sid);
                if (callback != null) {
                    log.error("callback is not null");
                    callback.reject(exception);
                }
            }
        }

    }

}