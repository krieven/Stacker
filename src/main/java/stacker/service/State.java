package stacker.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.Assert.*;

public class State<ArgumentT, ResultT, ReturnT, StateDataT, ResourcesT> {
    private static final ObjectMapper PARSER = new ObjectMapper();

    private Map<String, IHandler<ArgumentT, StateDataT, ResourcesT>> actionHandlers = new HashMap<>();
    private Map<String, ReturnHandler> returnHandlers = new HashMap<>();
    private IHandler<ArgumentT, StateDataT, ResourcesT> onOpen;

    private Service<?, ReturnT, StateDataT, ResourcesT> service;
    private Class<ArgumentT> argumentClass;

    public State(Class<ArgumentT> argumentClass) {
        this.argumentClass = argumentClass;
    }

    void setService(Service<?, ReturnT, StateDataT, ResourcesT> service) {
        this.service = service;
    }

    public void setOnOpen(IHandler<ArgumentT, StateDataT, ResourcesT> onOpen) {
        this.onOpen = onOpen;
    }

    void handleAction(String bodyString, ServiceContext<StateDataT, ResourcesT> context) {
        String action;
        ArgumentT argument;

        try {
            JsonNode tree = PARSER.readTree(bodyString);

            action = PARSER.writeValueAsString(tree.get("action")).trim().toUpperCase();
            argument = PARSER.treeToValue(tree.get("data"), argumentClass);

        } catch (IOException e) {
            // TODO return ERROR

            return;
        }
        IHandler<ArgumentT, StateDataT, ResourcesT> handler = actionHandlers.get(action);
        if (handler == null) {
            // TODO return ERROR

            return;
        }
        try {
            handler.handle(argument, context);
        } catch (Exception e) {
            // TODO return ERROR

            return;
        }
    }

    public void addActionHandler(String name, IHandler<ArgumentT, StateDataT, ResourcesT> handler) {
        assertNotNull("The name should not be null", name);
        name = name.trim().toUpperCase();
        assertNotEquals("The name should not be empty String", "", name);
        assertNotNull("The handler should not be null", handler);
        assertFalse("The handler with name '" + name + "' already exists", actionHandlers.containsKey(name));

        actionHandlers.put(name, handler);
    }

    void open(ArgumentT argument, ServiceContext<StateDataT, ResourcesT> context) {
        try {
            this.onOpen.handle(argument, context);
        } catch (Exception e) {
            //TODO send Error

            e.printStackTrace();
        }
    }

    private class ReturnHandler<T> {
        Class<T> returnClass;
        IHandler<T, StateDataT, ResourcesT> handler;
    }

    public <T> void handleReturn(String name, String body, ServiceContext<StateDataT, ResourcesT> context) {
        name = name.trim().toUpperCase();
        @SuppressWarnings("unchecked")
        ReturnHandler<T> returnHandler = returnHandlers.get(name);
        T returnValue;
        try {
            returnValue = PARSER.readValue(body, returnHandler.returnClass);
        } catch (IOException e) {
            //TODO send ERROR

            e.printStackTrace();
            return;
        }

        IHandler<T, StateDataT, ResourcesT> handler = returnHandler.handler;
        try {
            handler.handle(returnValue, context);
        } catch (Exception e) {
            //TODO send ERROR

            e.printStackTrace();
        }
    }

    public <T> void addReturnHandler(String name, Class<T> outerReturnClass, IHandler<T, StateDataT, ResourcesT> handler) {
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

    public void sendTransition(String name, ServiceContext<StateDataT, ResourcesT> context) {

    }

    public void sendResult(ResultT resultT, ServiceContext<StateDataT, ResourcesT> context) {

    }

    public void sendReturn(ReturnT returnT, ServiceContext<StateDataT, ResourcesT> context) {

    }

    public void sendOpen() {

    }

    public void sendError() {

    }
}