package stacker.router.server;

import stacker.common.AsyncServer;
import stacker.router.ISessionStorage;
import stacker.router.ITransport;
import stacker.router.Router;
import stacker.common.config.router.FlowConfig;
import stacker.common.config.router.NameMapping;
import stacker.common.config.router.RouterConfig;

public class RouterServer extends AsyncServer<RouterServlet> {

    private final ITransport transport;
    private final ISessionStorage sessionStorage;

    public RouterServer(ITransport transport, ISessionStorage sessionStorage, int port) {
        super(new RouterServlet(), port);
        this.transport = transport;
        this.sessionStorage = sessionStorage;
    }

    public void setConfig(RouterConfig config) {
        Router router = new Router(transport, sessionStorage);
        for (FlowConfig flowConfig : config.getFlows()) {
            router.addFlow(flowConfig.getName(), flowConfig.getAddress());
            for (NameMapping mapping : flowConfig.getMapping()) {
                router.setFlowMapping(flowConfig.getName(), mapping.getName(), mapping.getTarget());
            }
        }
        router.setMainFlow(config.getMainFlow());
        serviceServlet.setRouter(router);
    }

}
