package flow;

import io.github.krieven.stacker.flow.*;
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
                .withExit(AuthStateQuestion.Exits.FORWARD, "outerCall")
                .withTerminator(AuthStateQuestion.Exits.BACKWARD)
        );
        addState("outerCall", new OuterCall()
                .withTerminator(OuterCall.Exits.SUCCESS)
                .withExit(OuterCall.Exits.ERROR, "first")
        );
        setEnterState("first");
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

}
