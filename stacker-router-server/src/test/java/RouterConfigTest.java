import stacker.router.server.config.FlowConfig;
import stacker.router.server.config.NameMapping;
import stacker.router.server.config.RouterConfig;
import org.junit.Test;
import stacker.common.JsonParser;
import stacker.common.ParsingException;
import stacker.common.SerializingException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class RouterConfigTest {
    @Test
    public void test() throws SerializingException, ParsingException {
        RouterConfig routerConfig = new RouterConfig();
        routerConfig.setMainFlow("main");

        List<FlowConfig> flows = new ArrayList<FlowConfig>();
        routerConfig.setFlows(flows);

        FlowConfig flowConfig = new FlowConfig();
        flows.add(flowConfig);


        flowConfig.setName("main");
        flowConfig.setAddress("main-flow");

        NameMapping mapping = new NameMapping();
        mapping.setName("choose-car");
        mapping.setTarget("car-choose");

        List<NameMapping> flowMapping = new ArrayList<>();
        flowMapping.add(mapping);
        flowConfig.setMapping(flowMapping);


        String serialized = new String(new JsonParser().serialize(routerConfig));

        RouterConfig config = new JsonParser().parse(serialized.getBytes(), RouterConfig.class);

        assertNotNull(config);
    }

    @Test
    public void testFromJSON() throws ParsingException {
        RouterConfig config = new JsonParser().parse(
                ("{\n" +
                        "  \"flows\": [\n" +
                        "    {\n" +
                        "      \"name\": \"main\",\n" +
                        "      \"address\": \"main-address\",\n" +
                        "      \"mapping\": [\n" +
                        "        {\n" +
                        "          \"name\": \"other-flow\",\n" +
                        "          \"target\": \"other-flow-real-name\"\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"mainFlow\": \"main\"\n" +
                        "}").getBytes(),
                RouterConfig.class
        );
        assertNotNull(config);
    }
}
