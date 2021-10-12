package stacker.flow;

import stacker.common.Command;
import stacker.common.SerializingException;

/**
 * @param <Q> question type
 * @param <A> answer type
 * @param <F> flow data type
 * @param <E> exits enum
 */
public abstract class QuestionState<Q, A, F, E extends Enum<E>> extends InteractiveState<Q, A, F, E> {

    public QuestionState(Contract<Q, A> contract, E[] exits) {
        super(exits, contract);
    }

    @Override
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

}
