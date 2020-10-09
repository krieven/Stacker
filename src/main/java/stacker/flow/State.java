package stacker.flow;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public abstract class State<ArgumentT, ResultT, FlowDataT, ResourcesT> {

    private static Logger log = LoggerFactory.getLogger(State.class);

    private static final ObjectMapper PARSER = new ObjectMapper();

    private Map<String, OuterCallContract> outerCalls = new HashMap<>();

    protected Class<ArgumentT> argumentClass;

    public State(Class<ArgumentT> argumentClass) {
        this.argumentClass = argumentClass;
    }

    public abstract void onOpen(RequestContext<FlowDataT, ResourcesT> context);

    protected abstract void handleAction(String bodyString, RequestContext<FlowDataT, ResourcesT> context);

    final <A, R> void handleReturn(String name, String body, RequestContext<FlowDataT, ResourcesT> context) {
        name = name.trim().toUpperCase();
        @SuppressWarnings("unchecked")
        OuterCallContract<A, R> outerCallContract = outerCalls.get(name);
        R returnValue;
        try {
            returnValue = PARSER.readValue(body, outerCallContract.getReturnClass());
        } catch (IOException e) {
            context.sendError("BAD_MESSAGE_RETURNED");
            log.error("Error parsing return message", e);
            return;
        }

        IHandler<R, FlowDataT, ResourcesT> handler = outerCallContract.getHandler();
        try {
            handler.handle(returnValue, context);
        } catch (Exception e) {
            context.sendError("RETURN_PROCESSING_ERROR");
            log.error("Error processing return", e);
        }
    }

    public final <A, R> void addOuterCallContract(String name,
                                                  Class<A> argumentClass,
                                                  Class<R> returnClass,
                                                  IHandler<R, FlowDataT, ResourcesT> handler) {
        assertNotNull("NAME should not be null", name);
        name = name.trim().toUpperCase();
        assertNotEquals("NAME should not be empty String", "", name);
        assertFalse("The handler with name '" + name + "' already registered", outerCalls.containsKey(name));
        assertNotNull("The handler should not be null", handler);

        OuterCallContract<A, R> outerCallContract = new OuterCallContract<>();
        outerCallContract.setName(name);
        outerCallContract.setArgumentClass(argumentClass);
        outerCallContract.setReturnClass(returnClass);
        outerCallContract.setHandler(handler);
        outerCalls.put(name, outerCallContract);
    }

    public void sendResult(ResultT resultT, RequestContext<FlowDataT, ResourcesT> context) {
        context.sendResult(resultT);
    }

    public List<OuterCallContract> getOuterCallContracts() {
        Set<Map.Entry<String, OuterCallContract>> entries = outerCalls.entrySet();
        ArrayList<OuterCallContract> result = new ArrayList<>();
        for (Map.Entry<String, OuterCallContract> entry : entries) {
            result.add(entry.getValue());
        }
        return result;
    }

    public class OuterCallContract<A, R> {
        private String name;
        private Class<A> argumentClass;
        private Class<R> returnClass;
        private IHandler<R, FlowDataT, ResourcesT> handler;

        public String getName() {
            return name;
        }

        void setName(String name) {
            this.name = name;
        }

        public Class<A> getArgumentClass() {
            return argumentClass;
        }

        void setArgumentClass(Class<A> argumentClass) {
            this.argumentClass = argumentClass;
        }

        public Class<R> getReturnClass() {
            return returnClass;
        }

        void setReturnClass(Class<R> returnClass) {
            this.returnClass = returnClass;
        }

        IHandler<R, FlowDataT, ResourcesT> getHandler() {
            return handler;
        }

        void setHandler(IHandler<R, FlowDataT, ResourcesT> handler) {
            this.handler = handler;
        }
    }

}
