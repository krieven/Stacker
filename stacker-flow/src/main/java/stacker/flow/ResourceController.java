package stacker.flow;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stacker.common.dto.Command;

import java.util.List;
import java.util.Map;


public abstract class ResourceController<F> {
    private static final Logger log = LoggerFactory.getLogger(ResourceController.class);

    @NotNull
    protected abstract StateCompletion handle(
            List<String> pathInfo, Map<String, String[]> parameters, FlowContext<? extends F> context
    );

    protected abstract String getContentType();

    protected final StateCompletion sendResponse(byte[] resp, @NotNull FlowContext<? extends F> context) {

        return new StateCompletion(() -> context.getCallback().success(
                new Command() {
                    {
                        setType(Type.RESOURCE);
                        setBodyContentType(getContentType());
                        setContentBody(resp);
                    }
                }
        ));
    }
}
