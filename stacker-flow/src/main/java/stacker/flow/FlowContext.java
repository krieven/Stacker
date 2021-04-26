package stacker.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stacker.common.Command;
import stacker.common.ICallback;
import stacker.common.SerializingException;

/**
 * @param <F> FlowDataT
 * @param <R> ResourcesT
 */
public class FlowContext<F, R> {
    private static Logger log = LoggerFactory.getLogger(FlowContext.class);

    private String flowName;
    private String stateName;
    private F flowData;
    private R resources;

    private BaseFlow<?, ?, F, R> flow;
    private ICallback<Command> callback;

    FlowContext(BaseFlow<?, ?, F, R> flow,
                String flowName, String stateName,
                F flowData, R resources, ICallback<Command> callback
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

    public F getFlowData() {
        return flowData;
    }

    public R getResources() {
        return resources;
    }

    ICallback<Command> getCallback() {
        return callback;
    }

    BaseFlow<?, ?, F, R> getFlow() {
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