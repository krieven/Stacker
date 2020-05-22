package stacker.service.main;

public interface IHandler<ArgumentT, StateDataT, ResourcesT> {
    void handle(ArgumentT argument, RequestContext<StateDataT, ResourcesT> context);
}