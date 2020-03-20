package stacker.machine;

import stacker.Command;
import stacker.ICallback;

public class MachineContext<RsT, StateDataT, ResourcesT>{

    public MachineContext(
            Machine<Object, RsT, StateDataT, ResourcesT> machine,
            StateDataT stateData, ResourcesT resoutces, ICallback<Command> callback
    ){

    }

    public void sendResult(Object body, StateDataT session){

    }

}