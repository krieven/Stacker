package io.github.krieven.stacker.flow;

import javax.validation.constraints.NotNull;
import io.github.krieven.stacker.common.dto.Command;
import io.github.krieven.stacker.common.SerializingException;

/**
 * Subtype of InteractiveState and supertype of any States that should call external Workflows
 *
 * @param <Q> question type
 * @param <A> answer type
 * @param <F> flowData interface
 * @param <E> exits enum
 */
public abstract class StateOuterCall<Q, A, F, E extends Enum<E>> extends StateInteractive<Q, A, F, E> {

    /**
     * Constructs state
     *
     * @param outerCallContract the contract of external Workflow
     * @param exits declaration of exits from this state
     */
    public StateOuterCall(Contract<Q, A> outerCallContract, E[] exits) {
        super(exits, outerCallContract);
    }

    /**
     * Sends Question to external Workflow and stop
     *
     * @param question the Question
     * @param context this State current context
     * @return StateCompletion
     */
    @NotNull
    @Override
    public final StateCompletion sendQuestion(Q question, @NotNull FlowContext<? extends F> context) {
        Command command = new Command();
        command.setType(Command.Type.OPEN);
        command.setRqUid(context.getRqUid());
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
