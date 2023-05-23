package io.github.krieven.stacker.flow;

import io.github.krieven.stacker.flow.resource.ResourceLeaf;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.krieven.stacker.common.dto.Command;
import io.github.krieven.stacker.common.dto.ResourceRequest;

import java.util.function.BiConsumer;

public final class HandlerResource<F> implements BiConsumer<Command, FlowContext<F>> {
    private static final Logger log = LoggerFactory.getLogger(HandlerResource.class);

    @Override
    public void accept(@NotNull Command command, @NotNull FlowContext<F> context) {
        ResourceRequest request = command.getResourceRequest();

        ResourceLeaf<ResourceController<? super F>> leaf =
                context.getFlow().getResourceLeaf(context.getStateName(), request.getPath());
        if (leaf != null) {
            leaf.getResource().handle(leaf.getPathInfo(), request.getParameters(), context).doCompletion();
            return;
        }
        Command response = new Command();
        response.setType(Command.Type.RESOURCE);
        response.setBodyContentType("text/html");
        response.setContentBody("404 Resource not found".getBytes());

        context.getCallback().success(response);

    }
}
