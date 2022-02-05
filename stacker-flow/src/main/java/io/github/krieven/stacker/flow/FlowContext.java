package io.github.krieven.stacker.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.krieven.stacker.common.dto.Command;
import io.github.krieven.stacker.common.ICallback;
import io.github.krieven.stacker.common.SerializingException;

import java.util.Map;

/**
 * The FlowContext instance glues up Workflow data
 *
 * @param <F> FlowData type
 */
public class FlowContext<F> {
    private static final Logger log = LoggerFactory.getLogger(FlowContext.class);

    private final String flowName;
    private final String stateName;
    private final F flowData;

    private final BaseFlow<?, ?, F> flow;
    private final Map<String, String> properties;

    private final ICallback<Command> callback;

    FlowContext(BaseFlow<?, ?, F> flow,
                String flowName, String stateName,
                F flowData, Map<String, String> properties, ICallback<Command> callback
    ) {
        this.flowName = flowName;
        this.stateName = stateName;
        this.flow = flow;
        this.flowData = flowData;
        this.properties = properties;
        this.callback = callback;
    }

    /**
     * Provides the name of current flow in context of whole Process schema
     *
     * @return String - the name of current Workflow
     */
    public String getFlowName() {
        return flowName;
    }

    /**
     * Provides current State name in the context of current Workflow
     *
     * @return String - the current State name
     */
    public String getStateName() {
        return stateName;
    }

    /**
     * Provides FlowData of current Process in the current Workflow
     *
     * @return the instance of FlowData type
     */
    public F getFlowData() {
        return flowData;
    }

    /**
     * Provides configuration properties of the current Workflow in the current Process
     *
     * @return the Map of cofiguration properties
     */
    public Map<String, String> getProperties() {
        return properties;
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
        command.setBodyContentType(getFlow().getContract().getContentType());
        log.info("Flow {} ends on {} with command {}", getFlowName(), getStateName(), command);
        try {
            command.setContentBody(
                    getFlow().getContract().getParser().serialize(result)
            );
            if (getFlow().isDaemon(this)) {
                command.setFlowData(
                        getFlow().serializeFlowData(getFlowData())
                );
            }
        } catch (SerializingException e) {
            log.error("Flow {} cant send return value at {}", getFlowName(), getStateName());
            log.error("cause", e);
            getCallback().reject(e);
            return;
        }
        getCallback().success(command);
    }

    StateCompletion enterState(String name) {
        log.info("Flow {} entering state {}", getFlowName(), getStateName());
        return getFlow().enterState(name, this);
    }

}