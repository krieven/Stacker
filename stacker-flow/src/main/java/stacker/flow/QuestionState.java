package stacker.flow;

import stacker.common.Command;
import stacker.common.SerializingException;

public abstract class QuestionState<QuestionT, AnswerT, FlowDataI, ResourcesI, ExitsE extends Enum<ExitsE>> extends BaseState<FlowDataI, ResourcesI, ExitsE> {

    private TheContract<QuestionT, AnswerT> contract;

    public QuestionState(TheContract<QuestionT, AnswerT> contract, ExitsE[] exits) {
        super(exits);
        this.contract = contract;
    }

    void handle(byte[] answer, FlowContext<? extends FlowDataI, ? extends ResourcesI> context) {
        try {
            AnswerT value = contract.getParser().parse(answer, contract.getReturnClass());
            handleAnswer(value, context);
        } catch (Exception e) {
            context.getCallback().reject(e);
        }
    }

    public final void sendQuestion(QuestionT question, FlowContext<? extends FlowDataI, ? extends ResourcesI> context) {
        Command command = new Command();
        command.setType(Command.Type.QUESTION);
        command.setState(context.getStateName());
        try {
            command.setFlowData(context.getFlow().serializeFlowData(
                    context.getFlowData()
            ));
        } catch (SerializingException e) {
            context.getCallback().reject(e);
            return;
        }

        try {
            byte[] sResult = contract.getParser().serialize(question);
            command.setContentBody(sResult);
        } catch (SerializingException e) {
            context.getCallback().reject(e);
            return;
        }
        context.getCallback().success(command);

    }

    protected abstract void handleAnswer(AnswerT input, FlowContext<? extends FlowDataI, ? extends ResourcesI> context);

}
