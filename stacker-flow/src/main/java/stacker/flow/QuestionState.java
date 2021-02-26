package stacker.flow;

import org.jetbrains.annotations.NotNull;
import stacker.common.Command;
import stacker.common.SerializingException;

public abstract class QuestionState<QuestionT, AnswerT, FlowDataT, DaemonDataT, ResourcesT> extends AState<FlowDataT, DaemonDataT, ResourcesT> {

    private final Class<QuestionT> questionTClass;
    private final Class<AnswerT> answerTClass;

    public QuestionState(Class<QuestionT> questionTClass, Class<AnswerT> answerTClass) {
        this.questionTClass = questionTClass;
        this.answerTClass = answerTClass;
    }

    void handle(String answer, RequestContext<FlowDataT, DaemonDataT, ResourcesT> context) {
        try {
            AnswerT value = context.getFlow().getContract().getParser().parse(answer, answerTClass);
            handleAnswer(value, context);
        } catch (Exception e) {
            context.getCallback().reject(e);
        }
    }

    public final void sendQuestion(QuestionT question, @NotNull RequestContext<FlowDataT, DaemonDataT, ResourcesT> context) {
        Command command = new Command();
        command.setType(Command.Type.QUESTION);
        command.setState(context.getStateName());
        try {
            command.setFlowData(context.getFlow().serializeFlowData(
                    context.getFlowData()
            ));
            command.setDaemonData(context.getFlow().serializeFlowData(
                    context.getDaemonData()
            ));
        } catch (SerializingException e) {
            context.getCallback().reject(e);
            return;
        }

        try {
            String sResult = context.getFlow().getContract()
                    .getParser().serialize(question);
            command.setContentBody(sResult);
        } catch (SerializingException e) {
            context.getCallback().reject(e);
        }
        context.getCallback().success(command);

    }

    abstract void handleAnswer(AnswerT input, RequestContext<FlowDataT, DaemonDataT, ResourcesT> context);

    public Class<QuestionT> getQuestionTClass() {
        return questionTClass;
    }

    public Class<AnswerT> getAnswerTClass() {
        return answerTClass;
    }
}
