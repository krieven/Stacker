package stacker.router.server.config;

import java.util.ArrayList;
import java.util.List;

public class RouterConfig {
    private String mainFlow;
    private List<FlowConfig> flows = new ArrayList<FlowConfig>();

    public String getMainFlow() {
        return mainFlow;
    }

    public void setMainFlow(String mainFlow) {
        this.mainFlow = mainFlow;
    }

    public List<FlowConfig> getFlows() {
        return flows;
    }

    public void setFlows(List<FlowConfig> flows) {
        this.flows = flows;
    }
}
