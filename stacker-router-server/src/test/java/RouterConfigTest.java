import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.krieven.stacker.common.config.router.FlowConfig;
import io.github.krieven.stacker.common.config.router.NameMapping;
import io.github.krieven.stacker.common.config.router.RouterConfig;
import org.junit.Test;
import io.github.krieven.stacker.common.JsonParser;
import io.github.krieven.stacker.common.ParsingException;
import io.github.krieven.stacker.common.SerializingException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class RouterConfigTest {
    @Test
    public void test() throws SerializingException, ParsingException {
        RouterConfig routerConfig = new RouterConfig();
        routerConfig.setMainFlow("main");

        List<FlowConfig> flows = new ArrayList<>();
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

    @Test
    public void testFromFile() throws IOException {
        RouterConfig config = new ObjectMapper().readValue(
                readResource("router-config-view.json"),
                RouterConfig.class);

        assertNotNull(config);
    }

    void copy(InputStream source, OutputStream target) throws IOException {
        byte[] buf = new byte[8192];
        int length;
        while ((length = source.read(buf)) > 0) {
            target.write(buf, 0, length);
        }
    }

    byte[] readResource(String path) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = RouterConfigTest.class.getResourceAsStream(path);
        copy(in, out);
        return out.toByteArray();
    }
}
