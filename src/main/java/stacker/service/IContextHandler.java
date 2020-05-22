package stacker.service;

public interface IContextHandler<StateDataT, ResourcesT> {
    void handle(RequestContext<StateDataT, ResourcesT> context);
}
