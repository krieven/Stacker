package stacker.service;

public interface IContextHandler<StateDataT, ResourcesT> {
    void handle(ServiceContext<StateDataT, ResourcesT> context);
}
