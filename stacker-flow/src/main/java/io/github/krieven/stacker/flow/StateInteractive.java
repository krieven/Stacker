package io.github.krieven.stacker.flow;

import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import io.github.krieven.stacker.common.ParsingException;

import java.util.HashMap;
import java.util.Map;

public abstract class StateInteractive<Q, A, F, E extends Enum<E>> extends BaseState<F> {
    private final Map<Enum<?>, String> transitions = new HashMap<>();
    private final Enum<?>[] exits;

    private final Contract<Q, A> contract;

    StateInteractive(Enum<E>[] exits, Contract<Q, A> contract) {
        Assert.assertNotNull("exits should not be null", exits);
        Assert.assertNotNull("contract should not be null", contract);

        this.exits = exits;
        this.contract = contract;
    }


    @NotNull
    protected abstract StateCompletion sendQuestion(Q question, FlowContext<? extends F> context);

    @NotNull
    protected abstract StateCompletion handleAnswer(A answer, FlowContext<? extends F> context);

    @NotNull
    protected abstract StateCompletion onErrorParsingAnswer(FlowContext<? extends F> context);

    @Override
    void handle(byte[] answer, FlowContext<? extends F> context) {
        try {
            A value = getContract().parse(answer);
            handleAnswer(value, context).doCompletion();
        } catch (ParsingException e) {
            onErrorParsingAnswer(context).doCompletion();
        }
    }

    String getTransition(Enum<?> key) {
        return transitions.get(key);
    }

    public final StateInteractive<Q, A, F, E> withExit(E exit, String target) {
        target = target.trim().toUpperCase();
        if (transitions.containsKey(exit)) {
            throw new IllegalArgumentException("transition \"" + exit + "\" already defined");
        }
        transitions.put(exit, target);
        return this;
    }

    protected final StateCompletion exitState(E exit, @NotNull FlowContext<? extends F> context) {
        return context.enterState(getTransition(exit));
    }

    Enum<?>[] getExits() {
        return exits;
    }

    public final Contract<Q, A> getContract() {
        return contract;
    }

}
