package io.github.krieven.stacker.flow;

import org.jetbrains.annotations.NotNull;

/**
 *
 */
public final class StateTerminator<F> extends BaseState<F> {

    @NotNull
    @Override
    public StateCompletion onEnter(FlowContext<? extends F> context) {
        return new StateCompletion(context::sendReturn);
    }

    @Override
    void handle(byte[] answer, FlowContext<? extends F> context) {
        throw new IllegalStateException("Terminator does not handling answers");
    }

}
