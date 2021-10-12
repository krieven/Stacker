package stacker.flow;

import java.util.HashMap;
import java.util.Map;

/**
 * @param <F> flow data type
 * @param <E> exits enum
 */
public abstract class BaseState<F, E extends Enum<E>> {

    public abstract void onEnter(FlowContext<? extends F> context);

}
