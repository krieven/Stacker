package stacker.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stacker.common.dto.Command;
import stacker.common.ICallback;
import stacker.common.SerializingException;

/**
 * @param <F> FlowDataT
 */
public class FlowContext<F> {
    private static Logger log = LoggerFactory.getLogger(FlowContext.class);

    private String flowName;
    private String stateName;
    private F flowData;

    private BaseFlow<?, ?, F> flow;
    private ICallback<Command> callback;

    FlowContext(BaseFlow<?, ?, F> flow,
                String flowName, String stateName,
                F flowData, ICallback<Command> callback
    ) {
        this.flowName = flowName;
        this.stateName = stateName;
        this.flow = flow;
        this.flowData = flowData;
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

    ICallback<Command> getCallback() {
        return callback;
    }

    BaseFlow<?, ?, F> getFlow() {
        return flow;
    }

    void sendReturn() {
        Object result = getFlow().makeReturn(this);
        Command command = new Command();
        command.setType(Command.Type.RETURN);
        try {
            command.setContentBody(
                    getFlow().getContract()
                            .getParser().serialize(result)
            );
            if (getFlow().isDaemon()) {
                command.setDaemonData(
                        getFlow().serializeFlowData(getFlowData())
                );
            }
        } catch (SerializingException e) {
            getCallback().reject(e);
            return;
        }
        getCallback().success(command);
    }

    void enterState(String name) {
        getFlow().enterState(name, this);
    }

}