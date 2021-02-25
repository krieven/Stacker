package stacker.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stacker.common.Command;
import stacker.common.ICallback;

public class RequestContext<FlowDataT, DaemonDataT, ResourcesT> {
    private static Logger log = LoggerFactory.getLogger(RequestContext.class);

    private String flowName;
    private String stateName;
    private FlowDataT flowData;
    private DaemonDataT daemonData;
    private ResourcesT resources;

    private Flow<?, ?, FlowDataT, DaemonDataT, ResourcesT> flow;
    private ICallback<Command> callback;

    RequestContext(Flow<?, ?, FlowDataT, DaemonDataT, ResourcesT> flow,
                   String flowName, String stateName,
                   FlowDataT flowData, DaemonDataT daemonData, ResourcesT resources, ICallback<Command> callback
    ) {
        this.flowName = flowName;
        this.stateName = stateName;
        this.flow = flow;
        this.flowData = flowData;
        this.daemonData = daemonData;
        this.resources = resources;
        this.callback = callback;
    }

    public String getFlowName() {
        return flowName;
    }

    public String getStateName() {
        return stateName;
    }

    public FlowDataT getFlowData() {
        return flowData;
    }

    public DaemonDataT getDaemonData() {
        return daemonData;
    }

    public void setDaemonData(DaemonDataT daemonData) {
        this.daemonData = daemonData;
    }

    public ResourcesT getResources() {
        return resources;
    }

    ICallback<Command> getCallback() {
        return callback;
    }

    Flow<?, ?, FlowDataT, DaemonDataT, ResourcesT> getFlow() {
        return flow;
    }

}