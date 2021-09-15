package stacker.flow;

import java.util.HashMap;
import java.util.Map;

/**
 * @param <F> flow data type
 * @param <E> exits enum
 */
public abstract class BaseState<F, E extends Enum<E>> {

    private final Map<Enum<?>, String> transitions = new HashMap<>();

    private final Enum<?>[] exits;

    public BaseState(Enum<E>[] exits) {
        super();
        this.exits = exits;
    }

    public BaseState<F, E> withExit(E name, String target) {
        target = target.trim().toUpperCase();
        if (transitions.containsKey(name)) {
            throw new IllegalArgumentException("transition \"" + name + "\" already defined");
        }
        transitions.put(name, target);
        return this;
    }

    Enum<?>[] getExits() {
        return exits;
    }

    String getTransition(Enum<?> key) {
        return transitions.get(key);
    }

    public abstract void onEnter(FlowContext<? extends F> context);

    abstract void handle(byte[] answer, FlowContext<? extends F> context);

    public final void exitState(E target, FlowContext<? extends F> context) {
        context.sendTransition(getTransition(target));
    }

}
