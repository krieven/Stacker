package stacker.flow;

import org.jetbrains.annotations.NotNull;
import stacker.common.dto.Command;
import stacker.common.SerializingException;

/**
 * @param <Q> question type
 * @param <A> answer type
 * @param <F> flow data type
 * @param <E> enum of exits
 */
public abstract class StateQuestion<Q, A, F, E extends Enum<E>> extends StateInteractive<Q, A, F, E> {

    public StateQuestion(Contract<Q, A> contract, E[] exits) {
        super(exits, contract);
    }

    @NotNull
    @Override
    public final StateCompletion sendQuestion(Q question, @NotNull FlowContext<? extends F> context) {
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
            return new StateCompletion(() -> context.getCallback().reject(e));
        }

        try {
            byte[] sResult = getContract().serialize(question);
            command.setBodyContentType(getContract().getContentType());
            command.setBodyContentType(getContract().getContentType());
            command.setContentBody(sResult);
        } catch (SerializingException e) {
            return new StateCompletion(() -> context.getCallback().reject(e));
        }
        return new StateCompletion(() -> context.getCallback().success(command));
    }

    @NotNull
    @Override
    public StateCompletion onErrorParsingAnswer(FlowContext<? extends F> context) {
        return this.onEnter(context);
    }

    protected final void defineResourceController(String path, ResourceController<F> handler) {
        resourceControllers.put(path, handler);
    }
}
