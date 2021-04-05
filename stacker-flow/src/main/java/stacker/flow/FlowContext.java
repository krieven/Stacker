package stacker.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stacker.common.Command;
import stacker.common.ICallback;
import stacker.common.SerializingException;

public class FlowContext<FlowDataT, ResourcesT> {
    private static Logger log = LoggerFactory.getLogger(FlowContext.class);

    private String flowName;
    private String stateName;
    private FlowDataT flowData;
    private ResourcesT resources;

    private BaseFlow<?, ?, FlowDataT, ResourcesT> flow;
    private ICallback<Command> callback;

    FlowContext(BaseFlow<?, ?, FlowDataT, ResourcesT> flow,
                String flowName, String stateName,
                FlowDataT flowData, ResourcesT resources, ICallback<Command> callback
    ) {
        this.flowName = flowName;
        this.stateName = stateName;
        this.flow = flow;
        this.flowData = flowData;
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

    public ResourcesT getResources() {
        return resources;
    }

    ICallback<Command> getCallback() {
        return callback;
    }

    BaseFlow<?, ?, FlowDataT, ResourcesT> getFlow() {
        return flow;
    }

    void sendReturn() {
        Object returnT = getFlow().makeReturn(this);
        Command command = new Command();
        command.setType(Command.Type.RETURN);
        try {
            command.setContentBody(
                    getFlow().getContract()
                            .getParser().serialize(returnT)
            );
        } catch (SerializingException e) {
            getCallback().reject(e);
            return;
        }
        getCallback().success(command);
    }

    void sendTransition(String name) {
        getFlow().enterState(name, this);
    }

}