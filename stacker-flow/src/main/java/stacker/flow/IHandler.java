package stacker.flow;

public interface IHandler<ArgumentT, FlowDataT, DaemonDataT, ResourcesT> {
    void handle(ArgumentT argument, RequestContext<FlowDataT, DaemonDataT, ResourcesT> context) throws Exception;
}