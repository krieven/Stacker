package flow;

import states.auth.AuthState;
import stacker.common.JsonParser;
import stacker.flow.FlowContext;
import stacker.flow.BaseFlow;
import stacker.flow.TerminatorState;
import stacker.flow.Contract;

public class TestFlow extends BaseFlow<String, String, FlowData> {

    public TestFlow() {
        super(
                new Contract<>(
                        String.class,
                        String.class,
                        new JsonParser()
                ),
                FlowData.class,
                new JsonParser()
        );
    }

    @Override
    protected void configure() {
        addState("first", new AuthState()
                .withExit(AuthState.exits.FORWARD, "exit")
                .withExit(AuthState.exits.BACKWARD, "exit")
        );
        addState("exit", new TerminatorState<>());
    }

    @Override
    protected boolean isDaemon() {
        return false;
    }

    @Override
    protected FlowData createFlowData(String arg) {
        return new FlowData(arg);
    }

    @Override
    protected String makeReturn(FlowContext<FlowData> context) {
        return context.getFlowData().getAuthAnswer().getName();
    }

    @Override
    protected void onStart(FlowContext<FlowData> context) {
        enterState("first", context);
    }
}
