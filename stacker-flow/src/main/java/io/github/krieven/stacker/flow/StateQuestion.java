package io.github.krieven.stacker.flow;

import org.jetbrains.annotations.NotNull;
import io.github.krieven.stacker.common.dto.Command;
import io.github.krieven.stacker.common.SerializingException;

import java.util.function.BiFunction;

/**
 * @param <Q> question type
 * @param <A> answer type
 * @param <F> flow data type
 * @param <E> enum of exits
 */
public abstract class StateQuestion<Q, A, F, E extends Enum<E>> extends StateInteractive<Q, A, F, E> {

    private final BiFunction<Object, FlowContext<?>, Object> qWrapper;

    public StateQuestion(Contract<Q, A> contract, E[] exits) {
        this(contract, (q, c) -> q, exits);
    }

    public StateQuestion(Contract<Q, A> contract, BiFunction<Object, FlowContext<?>, Object> qWrapper, E[] exits) {
        super(exits, contract);
        this.qWrapper = qWrapper;
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
            byte[] sResult = getContract().getParser().serialize(qWrapper.apply(question, context));
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
