package stacker.common.config.router;

import java.util.ArrayList;
import java.util.List;

public class RouterConfig {
    private String name;
    private String title;
    private String description;
    private String mainFlow;
    private List<FlowConfig> flows = new ArrayList<>();

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
