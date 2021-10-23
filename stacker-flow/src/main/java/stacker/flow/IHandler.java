package stacker.flow;

import org.jetbrains.annotations.NotNull;

/**
 * @param <A> ArgumentT
 * @param <F> FlowDataT
 */
interface IHandler<A, F> {
    void handle(@NotNull A argument, @NotNull FlowContext<F> context) throws Exception;
}