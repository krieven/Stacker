package stacker.flow;


import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @param <F> flow data type
 */
public abstract class BaseState<F> {

    Map<String, ResourceController<F>> resourceControllers = new HashMap<>();
    private String name;

    @NotNull
    public abstract StateCompletion onEnter(FlowContext<? extends F> context);

    abstract void handle(byte[] answer, FlowContext<? extends F> context);

    void setName(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }
}
