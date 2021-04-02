package stacker.router.server;

import stacker.router.server.config.RouterConfig;
import stacker.router.ISessionStorage;
import stacker.router.ITransport;
import stacker.router.Router;

public class RouterHttpServer {

    private Router router;
    private ISessionStorage sessionStorage;
    private ITransport transport;
    private RouterConfig config;


    public RouterHttpServer withSessionStorage(ISessionStorage storage) {
        this.sessionStorage = storage;
        return this;
    }

    public RouterHttpServer withTransport(ITransport transport) {
        this.transport = transport;
        return this;
    }

    public RouterHttpServer withConfig(RouterConfig config) {
        this.config = config;
        return this;
    }

    private Router createRouter() {
        Router router = new Router(transport, sessionStorage);

        return router;
    }


    public void start() {
        if (sessionStorage == null || transport == null)
            throw new IllegalStateException("transport or sessionStorage not found");
        router = createRouter();

    }
}
