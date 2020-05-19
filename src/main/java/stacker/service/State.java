package stacker.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.junit.Assert.*;

public class State<ArgumentT, ResultT, StateDataT, ResourcesT> {
    private static Logger log = LogManager.getLogger(State.class);

    private static final ObjectMapper PARSER = new ObjectMapper();

    private Map<String, IHandler<ArgumentT, StateDataT, ResourcesT>> actionHandlers = new HashMap<>();
    private Map<String, ReturnHandler> returnHandlers = new HashMap<>();
    private IContextHandler<StateDataT, ResourcesT> initHandler;

    private Class<ArgumentT> argumentClass;

    public State(Class<ArgumentT> argumentClass) {
        this.argumentClass = argumentClass;
    }

    void handleInit(ServiceContext<StateDataT, ResourcesT> context) {
        try {
            this.initHandler.handle(context);
        } catch (Exception e) {
            context.sendError(e.getMessage());
            log.error("Error handling Init", e);
        }
    }

    public final void setInitHandler(IContextHandler<StateDataT, ResourcesT> init) {
        this.initHandler = init;
    }

    void handleAction(String bodyString, ServiceContext<StateDataT, ResourcesT> context) {
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
        IHandler<ArgumentT, StateDataT, ResourcesT> handler = actionHandlers.get(action);
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

    public final void addActionHandler(String name, IHandler<ArgumentT, StateDataT, ResourcesT> handler) {
        assertNotNull("The name should not be null", name);
        name = name.trim().toUpperCase();
        assertNotEquals("The name should not be empty String", "", name);
        assertNotNull("The handler should not be null", handler);
        assertFalse("The handler with name '" + name + "' already exists", actionHandlers.containsKey(name));

        actionHandlers.put(name, handler);
    }

    private class ReturnHandler<T> {
        Class<T> returnClass;
        IHandler<T, StateDataT, ResourcesT> handler;
    }

    final <T> void handleReturn(String name, String body, ServiceContext<StateDataT, ResourcesT> context) {
        name = name.trim().toUpperCase();
        @SuppressWarnings("unchecked")
        ReturnHandler<T> returnHandler = returnHandlers.get(name);
        T returnValue;
        try {
            returnValue = PARSER.readValue(body, returnHandler.returnClass);
        } catch (IOException e) {
            context.sendError("BAD_MESSAGE_RETURNED");
            log.error("Error parsing return message", e);
            return;
        }

        IHandler<T, StateDataT, ResourcesT> handler = returnHandler.handler;
        try {
            handler.handle(returnValue, context);
        } catch (Exception e) {
            context.sendError("RETURN_PROCESSING_ERROR");
            log.error("Error processing return", e);
        }
    }

    public final <T> void addReturnHandler(String name, Class<T> outerReturnClass, IHandler<T, StateDataT, ResourcesT> handler) {
        assertNotNull("NAME should not be null", name);
        name = name.trim().toUpperCase();
        assertNotEquals("NAME should not be empty String", "", name);
        assertNotNull("The outerReturn class should not be null", outerReturnClass);
        assertNotNull("The handler should not be null", handler);
        assertFalse("The handler with name '" + name + "' already registered", returnHandlers.containsKey(name));

        ReturnHandler<T> returnHandler = new ReturnHandler<>();
        returnHandler.returnClass = outerReturnClass;
        returnHandler.handler = handler;
        returnHandlers.put(name, returnHandler);
    }


    public final void sendResult(ResultT resultT, ServiceContext<StateDataT, ResourcesT> context) {
        context.sendResult(resultT);
    }

}