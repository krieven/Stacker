package stacker.flow.resource;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ResourceTree<T> {

    private Map<String, ResourceTree<T>> children = new HashMap<>();
    private T resource;

    public void add(String path, @NotNull T resource) {
        add(pathToList(path), resource);
    }

    public ResourceLeaf<T> find(String path) {
        return find(pathToList(path));
    }

    public T getResource() {
        return resource;
    }

    private void setResource(T resource) {
        if (this.resource == null) {
            this.resource = resource;
            return;
        }
        throw new IllegalArgumentException("path already used");
    }

    @Nullable
    private ResourceLeaf<T> find(LinkedList<String> path) {

        ResourceTree<T> lastFound = this;
        ResourceTree<T> current = this;
        List<String> pathInfo = new ArrayList<>();

        for (String part : path) {
            pathInfo.add(part);
            //todo current may be null
            if (current.children.containsKey(part) && current.children.get(part).hasResource()) {
                lastFound = current.children.get(part);
                pathInfo = new ArrayList<>();
            }
            current = current.children.get(part);
        }

        if (lastFound.hasResource()) {
            return new ResourceLeaf<>(pathInfo, lastFound.getResource());
        }
        return null;
    }

    private boolean hasResource() {
        return resource != null;
    }

    private void add(LinkedList<String> path, T resource) {
        if (path == null || path.size() == 0) {
            this.setResource(resource);
            return;
        }
        String childName = path.removeFirst();
        ResourceTree<T> child;
        if (!children.containsKey(childName)) {
            children.put(childName, new ResourceTree<>());
        }
        child = children.get(childName);
        child.add(path, resource);
    }

    private LinkedList<String> pathToList(String path) {
        if (path == null) {
            path = "";
        }
        path = path.trim();

        String[] parts = path.split("/");

        LinkedList<String> result = new LinkedList<>();

        for (String part : parts) {
            part = part.trim();
            if (part.length() == 0) continue;
            result.add(part);
        }
        return result;
    }

}
