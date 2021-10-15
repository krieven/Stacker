package stacker.flow;


/**
 * @param <F> flow data type
 */
public abstract class BaseState<F> {

    private BaseFlow<?, ?, ? extends F> flow;

    public abstract void onEnter(FlowContext<? extends F> context);

    public void setFlow(BaseFlow<?, ?, ? extends F> flow) {
        this.flow = flow;
    }

    public BaseFlow<?, ?, ? extends F> getFlow() {
        return flow;
    }
}
