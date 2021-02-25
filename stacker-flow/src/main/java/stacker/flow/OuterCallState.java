package stacker.flow;

import org.jetbrains.annotations.NotNull;
import stacker.common.Command;
import stacker.common.SerializingException;

public abstract class OuterCallState<QuestionT, AnswerT, FlowDataT, DaemonDataT, ResourcesT> extends AState<FlowDataT, DaemonDataT, ResourcesT> {

    private FlowContract outerCallContract;
    private String outerFlowName;

    public OuterCallState(String outerFlowName, @NotNull FlowContract<QuestionT, AnswerT> outerCallContract) {
        this.outerFlowName = outerFlowName;
        this.outerCallContract = outerCallContract;
    }

    public final void sendOuterCall(QuestionT question, @NotNull RequestContext<FlowDataT, DaemonDataT, ResourcesT> context) {
        Command command = new Command();
        command.setType(Command.Type.OPEN);
        command.setFlow(outerFlowName);
        command.setState(context.getStateName());
        try {
            command.setFlowData(
                    context.getFlow().serializeFlowData(context.getFlowData())
            );
            command.setDaemonData(
                    context.getFlow().serializeFlowData(context.getDaemonData())
            );

            command.setContentBody(
                    outerCallContract.getParser().serialize(question)
            );
        } catch (SerializingException e) {
            context.getCallback().reject(e);
        }
        context.getCallback().success(command);
    }

}
