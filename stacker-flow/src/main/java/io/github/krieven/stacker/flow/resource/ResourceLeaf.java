package io.github.krieven.stacker.flow.resource;

import javax.validation.constraints.NotNull;
import java.util.List;

public final class ResourceLeaf<T> {
    private final List<String> pathInfo;
    private final T resource;

    ResourceLeaf(@NotNull List<String> pathInfo, @NotNull T resource) {
        this.pathInfo = pathInfo;
        this.resource = resource;
    }

    public List<String> getPathInfo() {
        return pathInfo;
    }

    public T getResource() {
        return resource;
    }

}
