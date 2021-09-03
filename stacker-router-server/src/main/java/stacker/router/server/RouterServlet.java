package stacker.router.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stacker.common.AsyncServlet;
import stacker.common.ICallback;
import stacker.router.Router;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;


public class RouterServlet extends AsyncServlet {
    private static Logger log = LoggerFactory.getLogger(RouterServlet.class);

    private String cookieName = "CLOUD_BUNCH_SESSION_ID";

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

                if (request.getCookies() != null)
                    for (Cookie cookie : request.getCookies()) {
                        if (cookieName.equals(cookie.getName())) {
                            sid = cookie.getValue();
                            break;
                        }
                    }
                if (sid == null) {
                    sid = UUID.randomUUID().toString();
                    response.addCookie(new Cookie(cookieName, sid));
                }
                router.handleRequest(sid, bytes, new Router.IRouterCallback() {
                    @Override
                    public void success(String sid, byte[] body) {
                        try {
                            response.setHeader("Content-Length", String.valueOf(body.length));
                            writeBody(ctx, body);
                        } catch (IOException e) {
                            log.error(e.getMessage(), e);
                        }
                    }

                    @Override
                    public void reject(Exception exception) {
                        try {
                            writeBody(ctx, exception.getMessage().getBytes());
                        } catch (IOException e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                });
            }

            @Override
            public void reject(Exception error) {
                try {
                    writeBody(ctx, (error.getMessage() + "").getBytes());
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                    ctx.complete();
                }
            }
        });
    }

}
