package stacker.flow;

public interface IContext<F> {
    String getFlowName();

    String getStateName();

    F getFlowData();
}
