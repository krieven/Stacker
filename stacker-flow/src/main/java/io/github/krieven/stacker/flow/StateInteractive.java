package io.github.krieven.stacker.flow;

import javax.validation.constraints.NotNull;
import org.junit.Assert;
import io.github.krieven.stacker.common.ParsingException;

/**
 * Base class for all interactive States, can sendQuestion and handleAnswer
 *
 * @param <Q> question type
 * @param <A> answer type
 * @param <F> FlowData interface
 * @param <E> exits
 */
public abstract class StateInteractive<Q, A, F, E extends Enum<E>> extends State<F, E> {

    /**
     * the contract for interaction
     */
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

    /**
     * Sends Question for interaction and stop transition
     *
     * @param question the Question
     * @param context current State context
     * @return StateCompletion
     */
    @NotNull
    protected abstract StateCompletion sendQuestion(Q question, FlowContext<? extends F> context);

    /**
     * Handles Answer of interaction
     *
     * @param answer the Answer
     * @param context current State context
     * @return StateCompletion
     */
    @NotNull
    protected abstract StateCompletion handleAnswer(A answer, FlowContext<? extends F> context);

    /**
     * Determines - what to do if Answer is in the wrong format
     *
     * @param context current State context
     * @return StateCompletion
     */
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
