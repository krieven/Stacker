package io.github.krieven.stacker.router.server;

import io.github.krieven.stacker.router.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.krieven.stacker.common.AsyncServlet;
import io.github.krieven.stacker.common.ICallback;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;


public class RouterServlet extends AsyncServlet {
    private static final Logger log = LoggerFactory.getLogger(RouterServlet.class);

    private final String cookieName = "CLOUD_BUNCH_SESSION_ID";

    private Router router;

    public void setRouter(Router router) {
        this.router = router;
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final AsyncContext ctx = request.startAsync();
        readBody(ctx, new ICallback<byte[]>() {

            @Override
            public void success(byte[] bytes) {
                String sid = null;
                //TODO may change to request.getRequestedSessionId();
                if (request.getCookies() != null) {
                    for (Cookie cookie : request.getCookies()) {
                        if (cookieName.equals(cookie.getName())) {
                            sid = cookie.getValue();
                            break;
                        }
                    }
                }
                if (sid == null) {
                    sid = UUID.randomUUID().toString();
                    response.addCookie(new Cookie(cookieName, sid));
                }
                router.handleRequest(sid, bytes, new RouterCallback(ctx));
            }

            @Override
            public void reject(Exception error) {
                try {
                    writeBody(ctx, (error.getMessage() + "\n").getBytes());
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                    ctx.complete();
                }
            }
        });
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final AsyncContext ctx = request.startAsync();

        if (request.getCookies() == null) {
            writeBody(ctx, "400 bad Request".getBytes());
            return;
        }
        String sid = null;
        for (Cookie cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                sid = cookie.getValue();
                break;
            }
        }
        if (sid == null) {
            writeBody(ctx, "400 bad Request".getBytes());
            return;
        }

        router.handleResourceRequest(sid, request.getPathInfo(), request.getParameterMap(), new RouterCallback(ctx));

    }

    private class RouterCallback implements Router.IRouterCallback {

        private final AsyncContext ctx;

        private RouterCallback(AsyncContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void success(String sid, String contentType, byte[] body) {
            try {
                ctx.getResponse().setContentType(contentType);
                ctx.getResponse().setContentLength(body.length);
                writeBody(ctx, body);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                ctx.complete();
            }
        }

        @Override
        public void reject(Exception exception) {
            try {
                writeBody(ctx, exception.getMessage().getBytes());
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                ctx.complete();
            }
        }
    }
}
