package stacker.flow;

/**
 *
 */
public final class TerminatorState<F> extends BaseState<F> {

    @Override
    public void onEnter(FlowContext<? extends F> context) {
        context.sendReturn();
    }

}
