package stacker.flow;

/**
 * @param <A> ArgumentT
 * @param <F> FlowDataT
 */
interface IHandler<A, F> {
    void handle(A argument, FlowContext<F> context) throws Exception;
}