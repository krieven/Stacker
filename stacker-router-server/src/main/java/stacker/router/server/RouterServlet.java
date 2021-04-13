package stacker.router.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stacker.common.ICallback;
import stacker.router.Router;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

@WebServlet(asyncSupported = true,
        description = "Router servlet reference implementation"
)
public class RouterServlet extends HttpServlet {
    private static Logger log = LoggerFactory.getLogger(RouterServlet.class);

    private String cookieName;

    private Router router;

    public void setRouter(Router router) {
        this.router = router;
    }

    private void readBody(AsyncContext ctx, ICallback<byte[]> callback) throws IOException {
        ServletInputStream input = ctx.getRequest().getInputStream();

        input.setReadListener(new ReadListener() {

            final byte[] buf = new byte[1024];
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            @Override
            public void onDataAvailable() {
                try {
                    do {
                        int len = input.read(buf);
                        if (len < 0) return;
                        buffer.write(buf, 0, len);
                    } while (input.isReady());
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    callback.reject(e);
                }
            }

            @Override
            public void onAllDataRead() {
                callback.success(buffer.toByteArray());
            }

            @Override
            public void onError(Throwable throwable) {
                callback.reject(new Exception(throwable));
            }
        });
    }

    private void writeBody(AsyncContext ctx, byte[] body) throws IOException {
        ServletOutputStream output = ctx.getResponse().getOutputStream();

        output.setWriteListener(new WriteListener() {
            @Override
            public void onWritePossible() throws IOException {
                output.write(body);
                ctx.complete();
            }

            @Override
            public void onError(Throwable throwable) {
                log.error(throwable.getMessage(), throwable);
                ctx.complete();
            }
        });
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
                    writeBody(ctx, error.getMessage().getBytes());
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                    ctx.complete();
                }
            }
        });
    }

}
