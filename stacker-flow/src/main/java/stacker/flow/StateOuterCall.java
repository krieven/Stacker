package stacker.flow;

import org.jetbrains.annotations.NotNull;
import stacker.common.dto.Command;
import stacker.common.SerializingException;

/**
 * @param <Q> question type
 * @param <A> answer type
 * @param <F> flow data type
 * @param <E> exits enum
 */
public abstract class StateOuterCall<Q, A, F, E extends Enum<E>> extends StateInteractive<Q, A, F, E> {


    public StateOuterCall(Contract<Q, A> outerCallContract, E[] exits) {
        super(exits, outerCallContract);
    }

    @NotNull
    @Override
    public final StateCompletion sendQuestion(Q question, @NotNull FlowContext<? extends F> context) {
        Command command = new Command();
        command.setType(Command.Type.OPEN);
        command.setFlow(getName());
        command.setState(context.getStateName());
        try {
            command.setFlowData(
                    context.getFlow().serializeFlowData(
                            context.getFlowData()
                    )
            );
            command.setContentBody(
                    getContract().serialize(question)
            );
            command.setBodyContentType(getContract().getContentType());
        } catch (SerializingException e) {
            return new StateCompletion(() -> context.getCallback().reject(e));
        }

        return new StateCompletion(() -> context.getCallback().success(command));
    }

}
