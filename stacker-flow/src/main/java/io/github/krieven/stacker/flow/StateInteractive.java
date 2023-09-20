package io.github.krieven.stacker.flow;

import javax.validation.constraints.NotNull;
import org.junit.Assert;
import io.github.krieven.stacker.common.ParsingException;

public abstract class StateInteractive<Q, A, F, E extends Enum<E>> extends State<F, E> {

    private final Contract<Q, A> contract;

    StateInteractive(Enum<E>[] exits, Contract<Q, A> contract) {
        super(exits);
        Assert.assertNotNull("exits should not be null", exits);
        Assert.assertNotNull("contract should not be null", contract);

        this.contract = contract;
    }

    public final Contract<Q, A> getContract() {
        return contract;
    }

    @NotNull
    protected abstract StateCompletion sendQuestion(Q question, FlowContext<? extends F> context);

    @NotNull
    protected abstract StateCompletion handleAnswer(A answer, FlowContext<? extends F> context);

    @NotNull
    protected abstract StateCompletion onErrorParsingAnswer(FlowContext<? extends F> context);

    @Override
    final void handle(byte[] answer, FlowContext<? extends F> context) {
        try {
            A value = getContract().parse(answer);
            handleAnswer(value, context).doCompletion();
        } catch (ParsingException e) {
            onErrorParsingAnswer(context).doCompletion();
        }
    }
}
