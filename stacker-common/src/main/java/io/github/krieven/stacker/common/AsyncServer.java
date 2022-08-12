package io.github.krieven.stacker.common;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;

public class AsyncServer<S extends HttpServlet> {
    private static final Logger log = LoggerFactory.getLogger(AsyncServer.class);
    protected final S serviceServlet;
    private final Server server;
    private final ServletContextHandler contextHandler;

    public AsyncServer(S servlet, int port) {
        this.serviceServlet = servlet;

        ServletHolder servletHolder = new ServletHolder(serviceServlet);
        servletHolder.setAsyncSupported(true);

        contextHandler = new ServletContextHandler();
        contextHandler.setContextPath("/");
        contextHandler.addServlet(servletHolder, "/");

        server = new Server(port);
        server.setHandler(contextHandler);
    }

    public ServletContextHandler getContextHandler() {
        return contextHandler;
    }

    public void start() throws Exception {
        server.start();
        server.join();
    }
}
