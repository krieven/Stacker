package io.github.krieven.stacker.flow;


import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Base State class, all States are instances of this
 *
 * @param <F> flow data type interface
 */
public abstract class BaseState<F> {

    Map<String, ResourceController<F>> resourceControllers = new HashMap<>();
    private String name;

    /**
     * When Workflow enter the State, you should determine what to do.
     * You can call one of exitWith or sendQuestion method
     *
     * @param context - the context of current Workflow
     * @return StateCompletion
     */
    @NotNull
    public abstract StateCompletion onEnter(FlowContext<? extends F> context);

    abstract void handle(byte[] answer, FlowContext<? extends F> context);

    void setName(String name) {
        this.name = name;
    }

    /**
     * @return String - the name of this State in the Workflow schema
     */
    public String getName() {
        return name;
    }
}
