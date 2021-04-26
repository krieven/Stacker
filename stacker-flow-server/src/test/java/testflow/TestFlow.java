package testflow;

import stacker.common.JsonParser;
import stacker.flow.BaseFlow;
import stacker.flow.Contract;
import stacker.flow.FlowContext;

import java.util.HashMap;
import java.util.Map;

public class TestFlow extends BaseFlow<String, String, TestFlowData, Map<String, Object>> {

    public TestFlow() {
        super(
                new Contract<>(
                        String.class,
                        String.class,
                        new JsonParser()
                ),
                TestFlowData.class,
                new JsonParser(),
                new HashMap<>()
        );
    }

    @Override
    protected void configure() {

    }

    @Override
    protected TestFlowData createFlowData(String arg) {
        return new TestFlowData();
    }

    @Override
    protected String makeReturn(FlowContext<TestFlowData, Map<String, Object>> context) {
        return null;
    }

    @Override
    protected void onStart(FlowContext<TestFlowData, Map<String, Object>> context) {
        enterState("", context);
    }
}
