package stacker.flow.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stacker.common.*;
import stacker.common.dto.Command;
import stacker.flow.BaseFlow;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class FlowServlet extends AsyncServlet {
    private static Logger log = LoggerFactory.getLogger(FlowServlet.class);

    private IParser parser = new JsonParser();

    private BaseFlow<?, ?, ?> flow;

    FlowServlet(BaseFlow<?, ?, ?> flow) {
        super();
        this.flow = flow;
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
            log.error("error parsing command", e);

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
                Command errorCommand = new Command();
                errorCommand.setType(Command.Type.ERROR);
                errorCommand.setBodyContentType("text/html");
                errorCommand.setContentBody(("Command rejected by flow: " + " " +
                        error.getClass().getCanonicalName() + ", " +
                        error.getMessage()).getBytes());
                log.error("Command rejected by flow:", error);
                try {
                    byte[] body = parser.serialize(errorCommand);
                    ctx.getResponse().setContentType(flow.getContract().getContentType());

                    writeBody(ctx, body);
                    log.info(new String(errorCommand.getContentBody()));
                } catch (Exception e) {
                    log.error("Error writing error response", e);
                    ctx.complete();
                }
            }
        });
    }
}
