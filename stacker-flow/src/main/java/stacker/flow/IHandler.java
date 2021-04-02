package stacker.flow;

public interface IHandler<ArgumentT, FlowDataT, ResourcesT> {
    void handle(ArgumentT argument, FlowContext<FlowDataT, ResourcesT> context) throws Exception;
}