package stacker.flow;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stacker.common.dto.Command;
import stacker.common.dto.ResourceRequest;
import stacker.flow.resource.ResourceLeaf;

public final class HandlerResource<F> implements IHandler<Command, F> {
    private static final Logger log = LoggerFactory.getLogger(HandlerResource.class);

    @Override
    public void handle(@NotNull Command command, @NotNull FlowContext<F> context) {
        ResourceRequest request = command.getResourceRequest();

        ResourceLeaf<ResourceController<? super F>> leaf =
                context.getFlow().getResourceLeaf(context.getStateName(), request.getPath());
        if (leaf != null) {
            leaf.getResource().handle(leaf.getPathInfo(), request.getParameters(), context).doCompletion();
            return;
        }
        context.getCallback().success(
                new Command() {
                    {
                        setType(Type.RESOURCE);
                        setBodyContentType("text/html");
                        setContentBody("404 Resource not found".getBytes());
                    }
                }
        );

    }
}