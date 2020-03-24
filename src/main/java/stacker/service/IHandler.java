package stacker.service;

public interface IHandler<ArgumentT, StateDataT, ResourcesT> {
    void handle(ArgumentT argument, ServiceContext<StateDataT, ResourcesT> context) throws Exception;
}