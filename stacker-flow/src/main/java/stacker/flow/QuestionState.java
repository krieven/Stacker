package stacker.flow;

import stacker.common.dto.Command;
import stacker.common.SerializingException;

/**
 * @param <Q> question type
 * @param <A> answer type
 * @param <F> flow data type
 * @param <E> enum of exits
 */
public abstract class QuestionState<Q, A, F, E extends Enum<E>> extends InteractiveState<Q, A, F, E> {

    public QuestionState(Contract<Q, A> contract, E[] exits) {
        super(exits, contract);
    }

    protected abstract void configure(FlowContext<? extends F> context);

    @Override
    public final void sendQuestion(Q question, FlowContext<? extends F> context) {
        Command command = new Command();
        command.setType(Command.Type.QUESTION);
        command.setFlow(context.getFlowName());
        command.setState(context.getStateName());
        try {
            command.setFlowData(
                    getFlow().serializeFlowData(
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

    protected final void addResourceRequestHandler(String path, ResourceRequestHandler<? super F> handler, FlowContext<? extends F> context) {
        getFlow().addResourceRequestHandler(path, handler);
    }
}
