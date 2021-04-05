package stacker.flow;

public final class ReturnState<FlowDataT, ResourcesT, E extends Enum<E>> extends BaseState<FlowDataT, ResourcesT, E> {

    public ReturnState() {
        super(null);
    }

    @Override
    public void onEnter(FlowContext<? extends FlowDataT, ? extends ResourcesT> context) {
        context.sendReturn();
    }

    @Override
    void handle(byte[] answer, FlowContext<? extends FlowDataT, ? extends ResourcesT> context) {
    }

}
