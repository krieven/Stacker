package stacker.flow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stacker.Command;
import stacker.ICallback;

public class RequestContext<flowDataT, ResourcesT> {
    private static Logger log = LoggerFactory.getLogger(RequestContext.class);
    private static ObjectMapper PARSER = new ObjectMapper();

    private String flowName;
    private String stateName;
    private flowDataT flowData;
    private ResourcesT resources;

    private Flow<?, ?, flowDataT, ResourcesT> flow;
    private ICallback<Command> callback;

    RequestContext(Flow<?, ?, flowDataT, ResourcesT> flow,
                   String flowName, String stateName,
                   flowDataT flowData, ResourcesT resources, ICallback<Command> callback
    ) {
        this.flowName = flowName;
        this.stateName = stateName;
        this.flow = flow;
        this.flowData = flowData;
        this.resources = resources;
        this.callback = callback;
    }

    public String getFlowName() {
        return flowName;
    }

    public String getStateName() {
        return stateName;
    }

    public flowDataT getFlowData() {
        return flowData;
    }

    public ResourcesT getResources() {
        return resources;
    }

    ICallback<Command> getCallback() {
        return callback;
    }

    public void sendResult(Object result) {
        Command command = new Command();
        command.setCommand(Command.Type.RESULT);
        command.setState(stateName);
        try {
            command.setFlowData(PARSER.writeValueAsString(flowData));
        } catch (JsonProcessingException e) {
            log.error("Error serializing flowData", e);
        }

        try {
            command.setBody(PARSER.writeValueAsString(result));
        } catch (JsonProcessingException e) {
            log.error("Error serializing body", e);
        }
        callback.success(command);
    }

    public final void sendTransition(String name) {
        flow.sendTransition(name, this);
    }

    public void sendReturn() {
        Object returnT = flow.makeReturn(this);
        Command command = new Command();
        command.setCommand(Command.Type.RETURN);
        try {
            command.setBody(PARSER.writeValueAsString(returnT));
        } catch (JsonProcessingException e) {
            log.error("Error serializing returnT", e);
        }
        callback.success(command);
    }

    public void outerCall(String flow, Object arg, State.OuterCallContract outerCall) {
        Command command = new Command();
        command.setCommand(Command.Type.OPEN);
        command.setFlow(flow);
        command.setState(getStateName());
        command.setOnReturn(outerCall.getName());
        try {
            command.setFlowData(PARSER.writeValueAsString(flowData));
        } catch (JsonProcessingException e) {
            log.error("Error serializing flowData", e);
        }
        try {
            command.setBody(PARSER.writeValueAsString(arg));
        } catch (JsonProcessingException e) {
            log.error("Error serializing openArgument", e);
        }
        callback.success(command);
    }

    public void sendError(Object err) {
        Command command = new Command();
        command.setCommand(Command.Type.ERROR);
        ClientCommand body = new ClientCommand();

        body.command = Command.Type.ERROR;
        body.flow = flowName;
        body.state = stateName;
        body.data = err;

        try {
            command.setBody(PARSER.writeValueAsString(body));
        } catch (JsonProcessingException e) {
            log.error("Error serializing body", e);
        }
        callback.success(command);
    }


}