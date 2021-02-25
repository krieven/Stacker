import config.FlowConfig;
import config.RouterConfig;
import org.junit.Test;
import stacker.common.JsonParser;
import stacker.common.ParsingException;
import stacker.common.SerializingException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class RouterConfigTest {
    @Test
    public void test() throws SerializingException, ParsingException {
        RouterConfig routerConfig = new RouterConfig();
        routerConfig.setMainFlow("main");

        FlowConfig flowConfig = new FlowConfig();
        flowConfig.setAddress("http://main.flow.su");

        Map<String, String> mapping = new HashMap<>();
        mapping.put("doPayments", "payments");
        mapping.put("doTransfers", "transfers");
        flowConfig.setOuterCallMapping(mapping);

        Map<String, FlowConfig> flows = new HashMap<>();
        flows.put("main", flowConfig);

        routerConfig.setFlows(flows);

        String serialized = new JsonParser().serialize(routerConfig);

        RouterConfig config = new JsonParser().parse(serialized, RouterConfig.class);

        assertNotNull(config);
    }
}
