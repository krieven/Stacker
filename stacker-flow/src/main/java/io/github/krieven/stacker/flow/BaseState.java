package io.github.krieven.stacker.flow;

import org.jetbrains.annotations.NotNull;

/**
 * base class for State
 * @param <F> FlowData type
 */
abstract class BaseState<F> {
    private String name;

    /**
     * @return String - the name of this State in the Workflow schema
     */
    public final String getName() {
        return name;
    }

    /**
     * When Workflow enter the State, you should determine what to do.
     * You should call one of exitWith or sendQuestion method
     * or create your own StateCompletion
     *
     * @param context - the context of current Workflow
     * @return StateCompletion
     */
    @NotNull
    protected abstract StateCompletion onEnter(FlowContext<? extends F> context);

    void setName(String name) {
        this.name = name;
    }

    void handle(byte[] answer, FlowContext<? extends F> context) {
        throw new IllegalStateException("this method should not be called normally");
    }
}
