package testflow;

import stacker.common.JsonParser;
import stacker.flow.BaseFlow;
import stacker.flow.Contract;
import stacker.flow.FlowContext;


public class TestFlow extends BaseFlow<String, String, TestFlowData> {

    public TestFlow() {
        super(
                new Contract<>(
                        String.class,
                        String.class,
                        new JsonParser()
                ),
                TestFlowData.class,
                new JsonParser());
    }

    @Override
    protected void configure() {

    }

    @Override
    protected boolean isDaemon() {
        return false;
    }

    @Override
    protected TestFlowData createFlowData(String arg) {
        return new TestFlowData();
    }

    @Override
    protected String makeReturn(FlowContext<TestFlowData> context) {
        return null;
    }

    @Override
    protected void onStart(FlowContext<TestFlowData> context) {
        enterState("", context);
    }
}
