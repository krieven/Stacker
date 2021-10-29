package flow;

import org.jetbrains.annotations.NotNull;
import stacker.flow.*;
import states.auth.AuthState;
import stacker.common.JsonParser;

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

    @NotNull
    @Override
    protected StateCompletion onStart(FlowContext<FlowData> context) {
        return enterState("first", context);
    }
}
