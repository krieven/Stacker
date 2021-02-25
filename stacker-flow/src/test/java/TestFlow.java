import org.junit.Test;
import stacker.common.JsonParser;
import stacker.flow.Flow;
import stacker.flow.FlowContract;
import stacker.flow.RequestContext;

import java.util.HashMap;
import java.util.Map;

public class TestFlow {

    private class StringMap extends HashMap<String, String> {
    }

    @Test
    public Flow<String, String, StringMap, String, Map<String, String>> createFlow() {
        return new Flow<String, String, StringMap, String, Map<String, String>>(
                new FlowContract<>(String.class, String.class, new JsonParser()),
                StringMap.class,
                String.class,
                new JsonParser(),
                new StringMap()
        ) {
            @Override
            public void configure() {

            }

            @Override
            public StringMap createFlowData() {
                return new StringMap();
            }

            @Override
            public String createDaemonData() {
                return "";
            }

            @Override
            public String makeReturn(RequestContext<StringMap, String, Map<String, String>> context) {
                return context.getFlowData().get("some field");
            }

            @Override
            public void onOpen(String arg, RequestContext<StringMap, String, Map<String, String>> context) {
                sendTransition("", context);
            }
        };
    }
}
