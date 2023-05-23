package testflow;

import io.github.krieven.stacker.flow.*;
import org.jetbrains.annotations.NotNull;
import io.github.krieven.stacker.common.JsonParser;


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
    protected TestFlowData createFlowData(String arg, FlowContext<TestFlowData> context) {
        return new TestFlowData();
    }

    @Override
    protected String makeReturn(FlowContext<TestFlowData> context) {
        return null;
    }

}
