package stacker.service.main.states.start;

import stacker.service.main.Resources;
import stacker.service.State;
import stacker.service.main.StateData;

public class StateStart extends State<StateRq, StateRs, StateData, Resources> {

    {
        setOnOpenHandler(context -> {

            sendResult(null, context);
        });


    }

    public StateStart(Class<StateRq> argumentClass) {
        super(argumentClass);
    }

}
