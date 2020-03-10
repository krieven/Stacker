package stacker.machine;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class State<BodyT, RsT, SessionT, ResourcesT> {
    private Map<String, IHandler<BodyT, RsT, SessionT, ResourcesT>> actionHandlers = new HashMap<>();

    private Class<IBody<BodyT>> bodyClass;
    private static final ObjectMapper parser = new ObjectMapper();

    public State(Class<IBody<BodyT>> bodyClass) {
        this.bodyClass = bodyClass;
    }

    public void handle(String bodyString, MachineContext<RsT, SessionT, ResourcesT> context) {
        IBody<BodyT> body;
        try {
            body = parser.readValue(bodyString, bodyClass);
        } catch (IOException e) {
            // TODO return ERROR

            return;
        }
        String action = body.getAction();
        IHandler<BodyT, RsT, SessionT, ResourcesT> handler = actionHandlers.get(action);
        if (handler == null) {
            // TODO return ERROR

            return;
        }
        try {
            handler.handle(body.getData(), context);
        } catch (Exception e) {
            // TODO return ERROR

            return;
        }
    }

    public void addHandler(String name, IHandler<BodyT, RsT, SessionT, ResourcesT> handler) {
        assertNotNull("The name should not be null", name);
        assertNotEquals("The name should not be empty String", "", name.trim());
        assertNotNull("The handler should not be null", handler);
        name = name.trim();
        assertNull("The handler with name '" + name + "' already exists", actionHandlers.get(name));

        actionHandlers.put(name, handler);
    }

}