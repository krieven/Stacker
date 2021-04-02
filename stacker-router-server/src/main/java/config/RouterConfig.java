package config;

import java.util.Map;

public class RouterConfig {
    private Map<String, FlowConfig> flows;
    private String mainFlow;

    public Map<String, FlowConfig> getFlows() {
        return flows;
    }

    public void setFlows(Map<String, FlowConfig> flows) {
        this.flows = flows;
    }

    public String getMainFlow() {
        return mainFlow;
    }

    public void setMainFlow(String mainFlow) {
        this.mainFlow = mainFlow;
    }
}
