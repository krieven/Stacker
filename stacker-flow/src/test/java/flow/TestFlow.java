package flow;

import io.github.krieven.stacker.flow.*;
import org.jetbrains.annotations.NotNull;
import states.auth.AuthStateQuestion;
import io.github.krieven.stacker.common.JsonParser;
import states.outer.OuterCall;

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
        addState("first", new AuthStateQuestion()
                .withExit(AuthStateQuestion.exits.FORWARD, "outerCall")
                .withExit(AuthStateQuestion.exits.BACKWARD, "exit")
        );
        addState("outerCall", new OuterCall()
                .withExit(OuterCall.Exits.SUCCESS, "exit")
                .withExit(OuterCall.Exits.ERROR, "exit")
        );
        addState("exit", new StateTerminator<>());
    }

    @Override
    protected boolean isDaemon(FlowContext<FlowData> context) {
        return false;
    }

    @Override
    protected FlowData createFlowData(String arg, FlowContext<FlowData> context) {
        return new FlowData() {{
            setArgument(arg);
        }};
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
