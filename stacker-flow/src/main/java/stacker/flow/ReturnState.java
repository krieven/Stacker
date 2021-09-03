package stacker.flow;

/**
 * @param <F> flow data type
 * @param <E> exits enum
 */
public final class ReturnState<F, E extends Enum<E>> extends BaseState<F, E> {

    public ReturnState() {
        super(null);
    }

    @Override
    public void onEnter(FlowContext<? extends F> context) {
        context.sendReturn();
    }

    @Override
    void handle(byte[] answer, FlowContext<? extends F> context) {
        throw new java.lang.UnsupportedOperationException("Method 'handle' is not applicable for ReturnState");
    }

}
