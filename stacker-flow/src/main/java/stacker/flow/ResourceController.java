package stacker.flow;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stacker.common.dto.Command;

import java.util.List;
import java.util.Map;


public abstract class ResourceController<F> {
    private static final Logger log = LoggerFactory.getLogger(ResourceController.class);

    protected abstract void handle(
            List<String> path, Map<String, String> parameters, FlowContext<? extends F> context
    );

    protected abstract String getContentType();

    protected final void sendResponse(byte[] resp, @NotNull FlowContext<? extends F> context) {
        context.getCallback().success(
                new Command() {
                    {
                        setType(Type.RESOURCE);
                        setBodyContentType(getContentType());
                        setContentBody(resp);
                    }
                }
        );
    }
}
