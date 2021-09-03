package stacker.flow;

import stacker.common.Command;
import stacker.common.SerializingException;

/**
 * @param <Q> question type
 * @param <A> answer type
 * @param <F> flow data type
 * @param <E> exits enum
 */
public abstract class QuestionState<Q, A, F, E extends Enum<E>> extends BaseState<F, E> {

    private Contract<Q, A> contract;

    public QuestionState(Contract<Q, A> contract, E[] exits) {
        super(exits);
        this.contract = contract;
    }

    void handle(byte[] answer, FlowContext<? extends F> context) {
        try {
            A value = getContract().getParser().parse(answer, getContract().getAnswerType());
            handleAnswer(value, context);
        } catch (Exception e) {
            context.getCallback().reject(e);
        }
    }

    public final void sendQuestion(Q question, FlowContext<? extends F> context) {
        Command command = new Command();
        command.setType(Command.Type.QUESTION);
        command.setFlow(context.getFlowName());
        command.setState(context.getStateName());
        try {
            command.setFlowData(
                    context.getFlow().serializeFlowData(
                            context.getFlowData()
                    )
            );
        } catch (SerializingException e) {
            context.getCallback().reject(e);
            return;
        }

        try {
            byte[] sResult = getContract().getParser().serialize(question);
            command.setContentBody(sResult);
        } catch (SerializingException e) {
            context.getCallback().reject(e);
            return;
        }
        context.getCallback().success(command);

    }

    protected abstract void handleAnswer(A input, FlowContext<? extends F> context);

    public Contract<Q, A> getContract() {
        return contract;
    }
}
