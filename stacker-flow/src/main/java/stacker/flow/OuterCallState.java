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
public abstract class OuterCallState<Q, A, F, E extends Enum<E>> extends InteractiveState<Q, A, F, E> {

    private String outerFlowName;

    public OuterCallState(String outerFlowName, Contract<Q, A> outerCallContract, E[] exits) {
        super(exits, outerCallContract);
        this.outerFlowName = outerFlowName;
    }

    @Override
    public final void sendQuestion(Q question, @NotNull FlowContext<? extends F> context) {
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
                    getContract().serialize(question)
            );
        } catch (SerializingException e) {
            context.getCallback().reject(e);
            return;
        }
        context.getCallback().success(command);
    }


    public final String getOuterFlowName() {
        return outerFlowName;
    }

}
