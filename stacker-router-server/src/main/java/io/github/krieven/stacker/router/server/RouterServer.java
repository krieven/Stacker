package io.github.krieven.stacker.router.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.krieven.stacker.common.AsyncServer;
import io.github.krieven.stacker.router.ISessionStorage;
import io.github.krieven.stacker.router.ITransport;
import io.github.krieven.stacker.router.Router;
import io.github.krieven.stacker.common.config.router.RouterConfig;

public class RouterServer extends AsyncServer<RouterServlet> {
    private static Logger log = LoggerFactory.getLogger(RouterServer.class);

    private final ITransport transport;
    private final ISessionStorage sessionStorage;

    public RouterServer(ITransport transport, ISessionStorage sessionStorage, int port) {
        super(new RouterServlet(), port);
        this.transport = transport;
        this.sessionStorage = sessionStorage;
    }

    public boolean setConfig(RouterConfig config) {
        Router router = new Router(transport, sessionStorage);

        if (router.setConfig(config)) {
            serviceServlet.setRouter(router);
            log.info("new configuration have been applied");
            return true;
        }
        log.error("Configuration is not valid and not applied");
        return false;
    }

}
