package stacker.top;

import org.junit.Test;
import stacker.service.Service;
import stacker.service.ServiceContext;
import stacker.service.State;

public class ServiceTest {
    @Test
    public void newService() {
        Service<OpenArg, ReturnT, StateData, Resources> service =
                new Service<OpenArg, ReturnT, StateData, Resources>(OpenArg.class, StateData.class, new Resources()) {

                    @Override
                    public void init() {
                        State<OpenArg, ReturnT, StateData, Resources> state = new State<>(OpenArg.class);

                        state.setInitHandler(context -> {
                            context.sendTransition("main");
                        });

                        addState("start", state);
                    }

                    @Override
                    public StateData createStateData() {
                        return new StateData();
                    }

                    @Override
                    public void onOpen(OpenArg argument, ServiceContext<StateData, Resources> context) {
                        context.sendTransition("start");
                    }

                    @Override
                    public ReturnT makeReturn(ServiceContext<StateData, Resources> context) {
                        context.getStateData();
                        context.getResources();
                        return null;
                    }
                };


    }

    public static class OpenArg {
        public String name = "hello";
    }

    public static class ReturnT {
    }

    public static class StateData {
    }

    public static class Resources {

    }
}
