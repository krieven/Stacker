package stacker;

import org.junit.Test;
import stacker.service.Service;
import stacker.service.State;

public class ServiceTest {
    @Test
    public void newService() {
        Service<OpenArg, ReturnT, StateData, Resources> service =
                new Service<>(OpenArg.class, StateData.class, new Resources());

        service.setOnOpen((argument, context) -> {
            service.sendTransition("start", context);
        });

        State<OpenArg, ReturnT, ReturnT, StateData, Resources> state = service.addState("start",
                new State<>(OpenArg.class));
    }

    private static class OpenArg {
        public String name = "hello";
    }

    private class ReturnT {
    }

    private class StateData {
    }

    private class Resources {

    }
}
