package io.github.krieven.stacker.serve;

import io.github.krieven.stacker.common.ICallback;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WebServlet(asyncSupported = true,
        description = "Asynchronous servlet reference implementation"
)
public class AsyncServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(AsyncServlet.class);

    protected void readBody(AsyncContext ctx, ICallback<byte[]> callback) throws IOException {
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
                log.error(throwable.getMessage(), throwable);
                callback.reject(new Exception(throwable));
            }
        });
    }

    protected void writeBody(AsyncContext ctx, byte[] body) throws IOException {
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

    protected void writeError(AsyncContext ctx, int status, String message) throws IOException {
        ((HttpServletResponse) ctx.getResponse()).setStatus(status);
        writeBody(ctx, message.getBytes(StandardCharsets.UTF_8));
    }
}
