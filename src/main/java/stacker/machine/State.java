package stacker.machine;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class State<BodyT, RsT, StateDataT, ResourcesT> {
    private Map<String, IHandler<BodyT, RsT, StateDataT, ResourcesT>> actionHandlers = new HashMap<>();

    private Class<BodyT> bodyClass;
    private static final ObjectMapper PARSER = new ObjectMapper();

    public State(Class<BodyT> bodyClass) {
        this.bodyClass = bodyClass;
    }

    public void handle(String bodyString, MachineContext<RsT, StateDataT, ResourcesT> context) {
        String action;
        BodyT body;

        try {
            JsonNode tree = PARSER.readTree(bodyString);

            action = PARSER.writeValueAsString(tree.get("action"));
            body = PARSER.treeToValue(tree.get("data"), bodyClass);

        } catch (IOException e) {
            // TODO return ERROR

            return;
        }
        IHandler<BodyT, RsT, StateDataT, ResourcesT> handler = actionHandlers.get(action);
        if (handler == null) {
            // TODO return ERROR

            return;
        }
        try {
            handler.handle(body, context);
        } catch (Exception e) {
            // TODO return ERROR

            return;
        }
    }

    public void addHandler(String name, IHandler<BodyT, RsT, StateDataT, ResourcesT> handler) {
        assertNotNull("The name should not be null", name);
        name = name.trim();
        assertNotEquals("The name should not be empty String", "", name);
        assertNotNull("The handler should not be null", handler);
        assertNull("The handler with name '" + name + "' already exists", actionHandlers.get(name));

        actionHandlers.put(name, handler);
    }

}