package io.github.krieven.stacker.flow;

import javax.validation.constraints.NotNull;
import io.github.krieven.stacker.common.dto.Command;
import io.github.krieven.stacker.common.SerializingException;

import java.util.function.BiFunction;

/**
 * Subtype of InteractiveState and supertype of any States that should interact with client
 *
 * @param <Q> question type
 * @param <A> answer type
 * @param <F> flow data type
 * @param <E> enum of exits
 */
public abstract class StateQuestion<Q, A, F, E extends Enum<E>> extends StateInteractive<Q, A, F, E> {

    private final BiFunction<Q, FlowContext<?>, ?> qWrapper;

    /**
     * Constructs State with Contract and Exits
     *
     * @param contract the Contract
     * @param exits exits array
     */
    public StateQuestion(Contract<Q, A> contract, E[] exits) {
        this(contract, (q, c) -> q, exits);
    }

    /**
     * Constructs State with Contract, Exits and QuestionWrapper
     * Sometimes you need to wrap questions with additional data
     * such as flow name and state name
     *
     * @param contract the Contract
     * @param qWrapper wrapper function
     * @param exits exits array
     */
    public StateQuestion(Contract<Q, A> contract, BiFunction<Q, FlowContext<?>, ?> qWrapper, E[] exits) {
        super(exits, contract);
        this.qWrapper = qWrapper;
    }

    /**
     * Sends Question to client and stop
     *
     * @param question the Question
     * @param context current flow context
     * @return StateCompletion
     */
    @NotNull
    @Override
    public final StateCompletion sendQuestion(Q question, @NotNull FlowContext<? extends F> context) {
        Command command = new Command();
        command.setType(Command.Type.QUESTION);
        command.setRqUid(context.getRqUid());
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
            byte[] sResult = getContract().getParser().serialize(qWrapper.apply(question, context));
            command.setBodyContentType(getContract().getContentType());
            command.setBodyContentType(getContract().getContentType());
            command.setContentBody(sResult);
        } catch (SerializingException e) {
            return new StateCompletion(() -> context.getCallback().reject(e));
        }
        return new StateCompletion(() -> context.getCallback().success(command));
    }

    /**
     * Default implementation restart the State with current context
     *
     * @param context current State context
     * @return StateCompletion
     */
    @NotNull
    @Override
    public StateCompletion onErrorParsingAnswer(FlowContext<? extends F> context) {
        return this.onEnter(context);
    }

    /**
     * Map ResourceController of this state to path
     *
     * @param path path to map requests
     * @param handler the ResourceController
     */
    protected final void defineResourceController(String path, ResourceController<F> handler) {
        resourceControllers.put(path, handler);
    }
}
