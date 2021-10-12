package stacker.common;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;

public class AsyncServer<S extends HttpServlet> {
    private static final Logger log = LoggerFactory.getLogger(AsyncServer.class);
    protected final S serviceServlet;
    private final int port;
    private Server server;
    private ServletContextHandler contextHandler;

    public AsyncServer(S servlet, int port) {
        this.serviceServlet = servlet;
        this.port = port;
    }

    public ServletContextHandler getContextHandler() {
        return contextHandler;
    }

    public void start() throws Exception {
        contextHandler = new ServletContextHandler();
        contextHandler.setContextPath("/");

        ServletHolder servletHolder = new ServletHolder(serviceServlet);
        servletHolder.setAsyncSupported(true);
        contextHandler.addServlet(servletHolder, "/");

        server = new Server(port);
        server.setHandler(contextHandler);
        server.start();
        server.join();
    }
}
