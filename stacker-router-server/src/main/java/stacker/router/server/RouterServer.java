package stacker.router.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stacker.router.ISessionStorage;
import stacker.router.ITransport;
import stacker.router.Router;
import stacker.router.server.config.FlowConfig;
import stacker.router.server.config.NameMapping;
import stacker.router.server.config.RouterConfig;

public class RouterServer {
    private static Logger log = LoggerFactory.getLogger(RouterServer.class);

    private final ITransport transport;
    private final ISessionStorage sessionStorage;

    private RouterServlet routerServlet = new RouterServlet();

    private final int port;
    private Server server;

    public RouterServer(ITransport transport, ISessionStorage sessionStorage, int port) {
        this.transport = transport;
        this.sessionStorage = sessionStorage;
        this.port = port;
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
        routerServlet.setRouter(router);
    }

    public void start() throws Exception {
        ServletContextHandler contextHandler = new ServletContextHandler();
        contextHandler.setContextPath("/");

        ServletHolder servletHolder = new ServletHolder(routerServlet);
        servletHolder.setAsyncSupported(true);
        contextHandler.addServlet(servletHolder, "/");

        server = new Server(port);
        server.setHandler(contextHandler);
        server.start();
    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
