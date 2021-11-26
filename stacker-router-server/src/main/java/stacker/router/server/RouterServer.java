package stacker.router.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stacker.common.AsyncServer;
import stacker.router.ISessionStorage;
import stacker.router.ITransport;
import stacker.router.Router;
import stacker.common.config.router.RouterConfig;

public class RouterServer extends AsyncServer<RouterServlet> {
    private static Logger log = LoggerFactory.getLogger(RouterServer.class);

    private final ITransport transport;
    private final ISessionStorage sessionStorage;

    public RouterServer(ITransport transport, ISessionStorage sessionStorage, int port) {
        super(new RouterServlet(), port);
        this.transport = transport;
        this.sessionStorage = sessionStorage;
    }

    public void setConfig(RouterConfig config) {
        Router router = new Router(transport, sessionStorage);

        if (router.setConfig(config)) {
            serviceServlet.setRouter(router);
            log.info("new configuration have been applied");
            return;
        }
        log.error("Configuration is not valid and not applied");
    }

}
