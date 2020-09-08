package stacker.top;

import org.junit.Test;
import stacker.service.Service;
import stacker.service.RequestContext;

public class ServiceTest {
    @Test
    public void newService() {
        Service<OpenArg, ReturnT, StateData, Resources> service =
                new Service<OpenArg, ReturnT, StateData, Resources>(OpenArg.class, StateData.class, new Resources()) {

                    @Override
                    public void configure() {
                        setOnOpenHandler((argument, context) -> {
                            context.getStateData();
                        });
//                        addState("enter_name", null);

                    }

                    @Override
                    public StateData createStateData() {
                        return new StateData();
                    }

                    @Override
                    public ReturnT makeReturn(RequestContext<StateData, Resources> context) {
                        context.getStateData();
                        context.getResources();
                        return new ReturnT();
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
