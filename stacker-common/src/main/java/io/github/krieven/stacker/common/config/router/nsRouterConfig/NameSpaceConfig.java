package io.github.krieven.stacker.common.config.router.nsRouterConfig;

import java.util.Map;

public class NameSpaceConfig {
    private String title;
    private String description;
    private Map<String, FlowConfig> flowConfigMap;

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

    public Map<String, FlowConfig> getFlowConfigMap() {
        return flowConfigMap;
    }

    public void setFlowConfigMap(Map<String, FlowConfig> flowConfigMap) {
        this.flowConfigMap = flowConfigMap;
    }}
