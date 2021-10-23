package stacker.flow;


import java.util.HashMap;
import java.util.Map;

/**
 * @param <F> flow data type
 */
public abstract class BaseState<F> {

    Map<String, ResourceController<F>> resourceControllers = new HashMap<>();

    public abstract void onEnter(FlowContext<? extends F> context);

    abstract void handle(byte[] answer, FlowContext<? extends F> context);
}
