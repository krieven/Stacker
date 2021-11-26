package stacker.common;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;

public class AsyncServer<S extends HttpServlet> {
    private static final Logger log = LoggerFactory.getLogger(AsyncServer.class);
    protected final S serviceServlet;
    @SuppressWarnings("FieldCanBeLocal")
    private Server server;
    private final ServletContextHandler contextHandler;

    public AsyncServer(S servlet, int port) {
        this.serviceServlet = servlet;

        contextHandler = new ServletContextHandler();
        contextHandler.setContextPath("/");

        ServletHolder servletHolder = new ServletHolder(serviceServlet);

        server = new Server(port);
        server.setHandler(contextHandler);

        servletHolder.setAsyncSupported(true);
        contextHandler.addServlet(servletHolder, "/");

    }

    public ServletContextHandler getContextHandler() {
        return contextHandler;
    }

    public void start() throws Exception {
        server.start();
        server.join();
    }
}
