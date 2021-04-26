package stacker.flow;

/**
 * @param <Q> ArgumentT
 * @param <F> FlowDataT
 * @param <R> ResourcesT (AppContext)
 */
interface IHandler<Q, F, R> {
    void handle(Q argument, FlowContext<F, R> context) throws Exception;
}