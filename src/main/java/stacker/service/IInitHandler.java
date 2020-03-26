package stacker.service;

public interface IInitHandler<StateDataT, ResourcesT> {
    void handle(ServiceContext<StateDataT, ResourcesT> context);
}
