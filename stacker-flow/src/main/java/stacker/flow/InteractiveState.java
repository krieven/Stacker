package stacker.flow;

import org.junit.Assert;
import stacker.common.ParsingException;

import java.util.HashMap;
import java.util.Map;

public abstract class InteractiveState<Q, A, F, E extends Enum<E>> extends BaseState<F, E> {
    private final Map<Enum<?>, String> transitions = new HashMap<>();
    private final Enum<?>[] exits;

    private Contract<Q, A> contract;

    public InteractiveState(Enum<E>[] exits, Contract<Q, A> contract) {
        Assert.assertNotNull("exits should not be null", exits);
        Assert.assertNotNull("contract should not be null", contract);

        this.exits = exits;
        this.contract = contract;
    }

    public abstract void sendQuestion(Q question, FlowContext<? extends F> context);

    protected abstract void handleAnswer(A answer, FlowContext<? extends F> context);

    void handle(byte[] answer, FlowContext<? extends F> context) {
        try {
            A value = getContract().getParser().parse(answer, getContract().getAnswerType());
            handleAnswer(value, context);
        } catch (ParsingException e) {
            //todo handle it properly
            context.getCallback().reject(e);
        }
    }

    String getTransition(Enum<?> key) {
        return transitions.get(key);
    }

    public InteractiveState<Q, A, F, E> withExit(E exit, String target) {
        target = target.trim().toUpperCase();
        if (transitions.containsKey(exit)) {
            throw new IllegalArgumentException("transition \"" + exit + "\" already defined");
        }
        transitions.put(exit, target);
        return this;
    }

    protected final void exitState(E exit, FlowContext<? extends F> context) {
        context.sendTransition(getTransition(exit));
    }

    Enum<?>[] getExits() {
        return exits;
    }

    public Contract<Q, A> getContract() {
        return contract;
    }

}
