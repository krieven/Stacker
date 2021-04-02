package flow;

import auth.AuthState;
import stacker.common.JsonParser;
import stacker.flow.FlowContext;
import stacker.flow.BaseFlow;
import stacker.flow.ReturnState;
import stacker.flow.TheContract;

public class TestFlow extends BaseFlow<String, String, FlowData, Resources> {


    public TestFlow(Resources resources) {
        super(new TheContract<>(String.class, String.class, new JsonParser()),
                FlowData.class, new JsonParser(), resources);
    }

    @Override
    protected void configure() {
        addState("first", new AuthState()
                .withExit(AuthState.exits.FORWARD, "exit")
                .withExit(AuthState.exits.BACKWARD, "exit")
        );
        addState("exit", new ReturnState<>());
    }

    @Override
    protected FlowData createFlowData(String arg) {
        return new FlowData();
    }

    @Override
    protected String makeReturn(FlowContext<FlowData, Resources> context) {
        return context.getFlowData().getAuthAnswer().toString();
    }

    @Override
    protected void onStart(FlowContext<FlowData, Resources> context) {
        sendTransition("first", context);
    }
}
