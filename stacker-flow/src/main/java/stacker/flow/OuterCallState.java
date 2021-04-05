package stacker.flow;

import stacker.common.Command;
import stacker.common.ParsingException;
import stacker.common.SerializingException;

public abstract class OuterCallState<QuestionT, AnswerT, FlowDataI, ResourcesI, ExitsE extends Enum<ExitsE>> extends BaseState<FlowDataI, ResourcesI, ExitsE> {

    private Contract<QuestionT, AnswerT> outerCallContract;
    private String outerFlowName;

    public OuterCallState(String outerFlowName, Contract<QuestionT, AnswerT> outerCallContract, ExitsE[] exits) {
        super(exits);
        this.outerFlowName = outerFlowName;
        this.outerCallContract = outerCallContract;
    }

    void handle(byte[] answer, FlowContext<? extends FlowDataI, ? extends ResourcesI> context) {
        try {
            AnswerT value = getOuterCallContract().getParser().parse(answer, getOuterCallContract().getReturnClass());
            handleAnswer(value, context);
        } catch (ParsingException e) {
            context.getCallback().reject(e);
        }
    }

    public final void sendOuterCall(QuestionT question, FlowContext<? extends FlowDataI, ? extends ResourcesI> context) {
        Command command = new Command();
        command.setType(Command.Type.OPEN);
        command.setFlow(getOuterFlowName());
        command.setState(context.getStateName());
        try {
            command.setFlowData(
                    context.getFlow().serializeFlowData(
                            context.getFlowData()
                    )
            );
            command.setContentBody(
                    outerCallContract.getParser().serialize(question)
            );
        } catch (SerializingException e) {
            context.getCallback().reject(e);
            return;
        }
        context.getCallback().success(command);
    }

    protected abstract void handleAnswer(AnswerT answer, FlowContext<? extends FlowDataI, ? extends ResourcesI> context);

    public Contract<QuestionT, AnswerT> getOuterCallContract() {
        return outerCallContract;
    }

    public String getOuterFlowName() {
        return outerFlowName;
    }

}
