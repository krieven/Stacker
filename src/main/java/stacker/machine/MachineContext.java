package stacker.machine;

public class MachineContext<RsT, SessionT, ResourcesT>{

    public MachineContext(
            Machine<Object, RsT, SessionT, ResourcesT> machine,
            SessionT stateData, ResourcesT resoutces
    ){

    }

    public void sendResult(Object body, SessionT session){

    }

}