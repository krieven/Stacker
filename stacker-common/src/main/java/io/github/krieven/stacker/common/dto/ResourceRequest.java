package io.github.krieven.stacker.common.dto;

import java.io.Serializable;
import java.util.Map;

public class ResourceRequest implements Serializable {
    private String path;
    private Map<String, String[]> parameters;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String[]> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String[]> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "ResourceRequest{" +
                "path='" + path + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}
