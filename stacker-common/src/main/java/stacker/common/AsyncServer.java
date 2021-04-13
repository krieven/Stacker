package stacker.common;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;

public class AsyncServer<S extends HttpServlet> {
    private static Logger log = LoggerFactory.getLogger(AsyncServer.class);
    private final int port;
    protected S serviceServlet;
    private Server server;

    public AsyncServer(S servlet, int port) {
        this.serviceServlet = servlet;
        this.port = port;
    }

    public void start() throws Exception {
        ServletContextHandler contextHandler = new ServletContextHandler();
        contextHandler.setContextPath("/");

        ServletHolder servletHolder = new ServletHolder(serviceServlet);
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
