package io.github.krieven.stacker.flow;

import javax.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base State class, all States are instances of this
 * You can use it as superclass for hub States
 *
 * @param <F> flow data type interface
 * @param <E> possible exits declaration
 */
public abstract class State<F, E extends Enum<E>> extends BaseState<F> {

    private final Enum<?>[] exits;
    private final Map<Enum<?>, String> transitions = new HashMap<>();
    private final List<Enum<?>> terminators = new ArrayList<>();

    Map<String, ResourceController<F>> resourceControllers = new HashMap<>();

    /**
     * Constructs State with exits declaration
     * @param exits the array of exits declared
     */
    public State(Enum<E>[] exits) {
        this.exits = exits;
    }

    /**
     * define transition on exit from state
     *
     * @param exit Enum&lt;E&gt; the exit
     * @param target String the key of State to
     * @return this
     */
    public final State<F, E> withExit(Enum<E> exit, String target) {
        checkExitDefined(exit);
        transitions.put(exit, target.trim().toUpperCase());
        return this;
    }

    /**
     * define flow terminator on exit from state
     *
     * @param exit Enum&lt;E&gt; the exit
     * @return this
     */
    public final State<F, E> withTerminator(Enum<E> exit) {
        checkExitDefined(exit);
        terminators.add(exit);
        return this;
    }

    /**
     * You should call this method to exit from the state
     *
     * @param exit Enum&lt;E&gt; the exit
     * @param context State context
     * @return StateCompletion
     */
    protected final StateCompletion exitState(@NotNull Enum<E> exit, @NotNull FlowContext<? extends F> context) {
        String transition = getTransition(exit);
        if (transition != null) {
            return context.enterState(transition);
        }
        return new StateCompletion(context::sendReturn);
    }

    Enum<?>[] getExits() {
        return exits;
    }

    String getTransition(Enum<?> key) {
        return transitions.get(key);
    }

    boolean isTerminator(Enum<?> key) {
        return terminators.contains(key);
    }

    private void checkExitDefined(Enum<E> exit) {
        if (transitions.containsKey(exit) || terminators.contains(exit)) {
            throw new IllegalArgumentException("transition \"" + exit + "\" already defined");
        }
    }

}
