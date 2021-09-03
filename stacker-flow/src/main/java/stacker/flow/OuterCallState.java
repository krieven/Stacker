package stacker.flow;

import stacker.common.Command;
import stacker.common.ParsingException;
import stacker.common.SerializingException;

/**
 * @param <Q> question type
 * @param <A> answer type
 * @param <F> flow data type
 * @param <E> exits enum
 */
public abstract class OuterCallState<Q, A, F, E extends Enum<E>> extends BaseState<F, E> {

    private Contract<Q, A> outerCallContract;
    private String outerFlowName;

    public OuterCallState(String outerFlowName, Contract<Q, A> outerCallContract, E[] exits) {
        super(exits);
        this.outerFlowName = outerFlowName;
        this.outerCallContract = outerCallContract;
    }

    void handle(byte[] answer, FlowContext<? extends F> context) {
        try {
            A value = getOuterCallContract().getParser().parse(answer, getOuterCallContract().getAnswerType());
            handleAnswer(value, context);
        } catch (ParsingException e) {
            context.getCallback().reject(e);
        }
    }

    public final void sendOuterCall(Q question, FlowContext<? extends F> context) {
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

    protected abstract void handleAnswer(A answer, FlowContext<? extends F> context);

    public Contract<Q, A> getOuterCallContract() {
        return outerCallContract;
    }

    public String getOuterFlowName() {
        return outerFlowName;
    }

}
