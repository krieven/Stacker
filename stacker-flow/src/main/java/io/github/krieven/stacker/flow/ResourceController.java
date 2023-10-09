package io.github.krieven.stacker.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.krieven.stacker.common.dto.Command;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * Resource controller for the Question State
 * @param <F> flowData type
 */
public abstract class ResourceController<F> {
    private static final Logger log = LoggerFactory.getLogger(ResourceController.class);

    /**
     * Handle request to context of current State
     *
     * @param pathInfo chunked pathInfo
     * @param parameters parameters of the request
     * @param context current State context
     * @return StateCompletion
     */
    @NotNull
    protected abstract StateCompletion handle(
            List<String> pathInfo, Map<String, String[]> parameters, FlowContext<? extends F> context
    );

    /**
     * define content type of response
     * @return content type of response
     */
    protected abstract String getContentType();

    /**
     * Call this method to send response to the client
     * @param resp response as bytes
     * @param context current State context
     * @return StateCompletion
     */
    protected final StateCompletion sendResponse(byte[] resp, @NotNull FlowContext<? extends F> context) {
        Command command = new Command();
        command.setType(Command.Type.RESOURCE);
        command.setRqUid(context.getRqUid());
        command.setBodyContentType(getContentType());
        command.setContentBody(resp);
        return new StateCompletion(() -> context.getCallback().success(command));
    }
}
