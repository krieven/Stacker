package stacker.flow;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stacker.Command;

import static org.junit.Assert.*;

public abstract class ActionState<ArgumentT, ResultT, FlowDataT, ResourcesT> extends State<ArgumentT, ResultT, FlowDataT, ResourcesT> {
    private static Logger log = LoggerFactory.getLogger(ActionState.class);

    private static final ObjectMapper PARSER = new ObjectMapper();

    private Map<String, IHandler<ArgumentT, FlowDataT, ResourcesT>> actionHandlers = new HashMap<>();

    public ActionState(Class<ArgumentT> argumentClass) {
        super(argumentClass);
    }

    @Override
    protected void handleAction(String bodyString, RequestContext<FlowDataT, ResourcesT> context) {
        String action;
        ArgumentT argument;

        try {
            JsonNode tree = PARSER.readTree(bodyString);

            action = PARSER.writeValueAsString(tree.get("action")).trim().toUpperCase();
            argument = PARSER.treeToValue(tree.get("data"), argumentClass);

        } catch (IOException e) {
            context.sendError("BAD_MESSAGE_FORMAT");
            return;
        }
        IHandler<ArgumentT, FlowDataT, ResourcesT> handler = actionHandlers.get(action);
        if (handler == null) {
            context.sendError("ACTION_NOT_FOUND");
            return;
        }
        try {
            handler.handle(argument, context);
        } catch (Exception e) {
            context.sendError(e.getMessage());
        }
    }

    public final void addActionHandler(String name, IHandler<ArgumentT, FlowDataT, ResourcesT> handler) {
        assertNotNull("The name should not be null", name);
        name = name.trim().toUpperCase();
        assertNotEquals("The name should not be empty String", "", name);
        assertNotNull("The handler should not be null", handler);
        assertFalse("The handler with name '" + name + "' already exists", actionHandlers.containsKey(name));

        actionHandlers.put(name, handler);
    }

    public void sendResult(ResultT result, RequestContext<FlowDataT, ResourcesT> context) {
        ClientCommand body = new ClientCommand();

        body.command = Command.Type.RESULT;
        body.state = context.getStateName();
        body.flow = context.getFlowName();
        body.data = result;

        context.sendResult(body);
    }

}