package stacker.service.states.start;

import stacker.service.*;

public class StateStart extends State<StateRq, StateRs, StateData, Resources> {

    {
        setInitHandler(context -> {

            sendResult(null, context);
        });


    }

    public StateStart(Class<StateRq> argumentClass) {
        super(argumentClass);
    }

}
