package stacker.flow;

/**
 *
 */
public final class TerminatorState<F, E extends Enum<E>> extends BaseState<F, E> {

    public TerminatorState() {
    }

    @Override
    public void onEnter(FlowContext<? extends F> context) {
        context.sendReturn();
    }

}
