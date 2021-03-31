package flow;

import auth.AuthorizingQuestion;
import stacker.common.JsonParser;
import stacker.flow.FlowContext;
import stacker.flow.BaseFlow;
import stacker.flow.TheContract;

import java.util.HashMap;

public class TestFlow extends BaseFlow<String, String, FlowData, HashMap> {


    public TestFlow(HashMap resources) {
        super(new TheContract<>(String.class, String.class, new JsonParser()),
                FlowData.class, new JsonParser(), resources);
    }

    @Override
    public void configure() {
        addState("first", new AuthorizingQuestion());
    }

    @Override
    public FlowData createFlowData(String arg) {
        return null;
    }

    @Override
    public String makeReturn(FlowContext<FlowData, HashMap> context) {
        return null;
    }

    @Override
    public void onStart(FlowContext<FlowData, HashMap> context) {
        sendTransition("first", context);
    }
}
