package stacker.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stacker.common.dto.Command;
import stacker.common.ICallback;
import stacker.common.SerializingException;

/**
 * @param <F> FlowDataT
 */
public class FlowContext<F> implements IContext<F> {
    private static final Logger log = LoggerFactory.getLogger(FlowContext.class);

    private final String flowName;
    private final String stateName;
    private final F flowData;

    private final BaseFlow<?, ?, F> flow;
    private final ICallback<Command> callback;

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

    @Override
    public String getFlowName() {
        return flowName;
    }

    @Override
    public String getStateName() {
        return stateName;
    }

    @Override
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
                    getFlow().getContract().getParser().serialize(result)
            );
            if (getFlow().isDaemon()) {
                command.setFlowData(
                        getFlow().serializeFlowData(getFlowData())
                );
            }
        } catch (SerializingException e) {
            getCallback().reject(e);
            return;
        }
        getCallback().success(command);
    }

    StateCompletion enterState(String name) {
        return getFlow().enterState(name, this);
    }

}