package stacker.service.main;

public interface IContextHandler<StateDataT, ResourcesT> {
    void handle(RequestContext<StateDataT, ResourcesT> context);
}
