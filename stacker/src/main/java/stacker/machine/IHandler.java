package stacker.machine;

public interface IHandler<BodyT, RsT, SessionT, ResourcesT> {
    void handle(BodyT request, MachineContext<RsT, SessionT, ResourcesT> context) throws Exception;
}