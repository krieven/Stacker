package io.github.krieven.stacker.common.config.router;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NameSpace {
    private String name;
    private String title;
    private String description;
    private List<FlowConfig> flows = new ArrayList<>();
    private Map<String, String> export;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<FlowConfig> getFlows() {
        return flows;
    }

    public void setFlows(List<FlowConfig> flows) {
        this.flows = flows;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getExport() {
        return export;
    }

    public void setExport(Map<String, String> export) {
        this.export = export;
    }
}
