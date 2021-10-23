package stacker.flow;

/**
 *
 */
public final class TerminatorState<F> extends BaseState<F> {

    @Override
    public void onEnter(FlowContext<? extends F> context) {
        context.sendReturn();
    }

    @Override
    void handle(byte[] answer, FlowContext<? extends F> context) {
        throw new IllegalStateException("Terminator does not handling answers");
    }

}
