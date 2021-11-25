package testflow;

import org.jetbrains.annotations.NotNull;
import stacker.common.JsonParser;
import stacker.flow.BaseFlow;
import stacker.flow.Contract;
import stacker.flow.FlowContext;
import stacker.flow.StateCompletion;


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
    protected boolean isDaemon(FlowContext<TestFlowData> context) {
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

    @NotNull
    @Override
    protected StateCompletion onStart(FlowContext<TestFlowData> context) {
        return enterState("", context);
    }
}
