package io.github.krieven.stacker.serve;

import jakarta.servlet.http.HttpServlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;


public class AsyncServer<S extends HttpServlet> {
    protected final S serviceServlet;
    private final Server server;

    public AsyncServer(S servlet, int port) {
        this.serviceServlet = servlet;

        ServletHolder servletHolder = new ServletHolder(serviceServlet);
        servletHolder.setAsyncSupported(true);

        ServletContextHandler contextHandler = new ServletContextHandler();
        contextHandler.setContextPath("/");
        contextHandler.addServlet(servletHolder, "/");

        server = new Server(port);
        server.setHandler(contextHandler);
    }

    public void start() throws Exception {
        server.setStopAtShutdown(true);
        server.start();
        server.join();
    }

}
