package io.github.krieven.stacker.flow.server;

import io.github.krieven.stacker.common.*;
import io.github.krieven.stacker.flow.FlowHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.krieven.stacker.common.dto.Command;
import io.github.krieven.stacker.flow.BaseFlow;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class FlowServlet extends AsyncServlet {
    private static final Logger log = LoggerFactory.getLogger(FlowServlet.class);

    private final IParser parser = new JsonParser();

    private final FlowHandler flow;

    FlowServlet(BaseFlow<?, ?, ?> flow) {
        super();
        this.flow = FlowHandler.create(flow);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.getWriter().print("{\"status\":\"" + (flow != null ? "ok" : "bad") + "\"}");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final AsyncContext ctx = request.startAsync();
        readBody(ctx, new ICallback<byte[]>() {

            @Override
            public void success(byte[] bytes) {
                onBodyReceived(ctx, bytes);
            }

            @Override
            public void reject(Exception error) {
                log.error("error reading body", error);
            }
        });
    }

    public void onBodyReceived(AsyncContext ctx, byte[] bytes) {
        Command command;
        try {
            command = parser.parse(bytes, Command.class);
        } catch (ParsingException e) {
            handleError(e, ctx);
            return;
        }
        flow.handleCommand(command, new ICallback<Command>() {
            @Override
            public void success(Command command) {
                byte[] body;
                try {
                    body = parser.serialize(command);
                } catch (SerializingException se) {
                    body = "Error serializing command".getBytes();
                    log.error("Error serializing command", se);
                }

                try {
                    writeBody(ctx, body);
                } catch (Exception e) {
                    log.error("Error writing response", e);
                    ctx.complete();
                }

            }

            @Override
            public void reject(Exception error) {
                log.error("requested {} flow '{}' state '{}' rejected with {}", command.getType(), command.getFlow(), command.getState(), error.toString());
                handleError(error, ctx);
            }
        });
    }

    private void handleError(Throwable error, AsyncContext ctx) {
        Command errorCommand = new Command();
        errorCommand.setType(Command.Type.ERROR);
        errorCommand.setBodyContentType("text/html");
        errorCommand.setContentBody(("Command rejected by flow: " + " " +
                error.getClass().getCanonicalName() + ", " +
                error.getMessage()).getBytes());
        log.error("Command rejected by flow:", error);
        try {
            byte[] body = parser.serialize(errorCommand);
            ctx.getResponse().setContentType(flow.getFlow().getContract().getContentType());

            writeBody(ctx, body);
            log.info(new String(errorCommand.getContentBody()));
        } catch (Exception e) {
            log.error("Error writing error response", e);
            ctx.complete();
        }
    }
}
