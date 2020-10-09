package stacker.flow;

public interface IHandler<ArgumentT, flowDataT, ResourcesT> {
    void handle(ArgumentT argument, RequestContext<flowDataT, ResourcesT> context);
}