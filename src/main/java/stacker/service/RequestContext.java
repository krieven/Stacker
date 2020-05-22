package stacker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import stacker.Command;
import stacker.ICallback;

public class RequestContext<StateDataT, ResourcesT> {
    private static ObjectMapper PARSER = new ObjectMapper();

    private String serviceName;
    private String stateName;
    private StateDataT stateData;
    private ResourcesT resources;

    private Service<?, ?, StateDataT, ResourcesT> service;
    private ICallback<Command> callback;

    RequestContext(Service<?, ?, StateDataT, ResourcesT> service,
                   String serviceName, String stateName,
                   StateDataT stateData, ResourcesT resources, ICallback<Command> callback
    ) {
        this.serviceName = serviceName;
        this.stateName = stateName;
        this.service = service;
        this.stateData = stateData;
        this.resources = resources;
        this.callback = callback;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getStateName() {
        return stateName;
    }

    public StateDataT getStateData() {
        return stateData;
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
            command.setStateData(PARSER.writeValueAsString(stateData));
        } catch (JsonProcessingException e) {
            //TODO do something
            e.printStackTrace();
        }

        ClientCommand body = new ClientCommand();

        body.command = Command.Type.RESULT;
        body.state = stateName;
        body.service = serviceName;
        body.data = result;

        try {
            command.setBody(PARSER.writeValueAsString(body));
        } catch (JsonProcessingException e) {
            //TODO do something
            e.printStackTrace();
        }
        callback.success(command);
    }

    public final void sendTransition(String name) {
        service.sendTransition(name, this);
    }

    public void sendReturn() {
        Object returnT = service.makeReturn(this);
        Command command = new Command();
        command.setCommand(Command.Type.RETURN);
        try {
            command.setBody(PARSER.writeValueAsString(returnT));
        } catch (JsonProcessingException e) {
            //TODO do something
            e.printStackTrace();
        }
        callback.success(command);
    }

    public void sendOpen(String service, Object arg, String onReturn) {
        Command command = new Command();
        command.setCommand(Command.Type.OPEN);
        command.setService(service);
        command.setState(getStateName());
        command.setOnReturn(onReturn);
        try {
            command.setStateData(PARSER.writeValueAsString(stateData));
        } catch (JsonProcessingException e) {
            //TODO do something
            e.printStackTrace();
        }
        try {
            command.setBody(PARSER.writeValueAsString(arg));
        } catch (JsonProcessingException e) {
            //TODO do something
            e.printStackTrace();
        }
        callback.success(command);
    }

    public void sendError(Object err) {
        Command command = new Command();
        command.setCommand(Command.Type.ERROR);
        ClientCommand body = new ClientCommand();

        body.command = Command.Type.ERROR;
        body.service = serviceName;
        body.state = stateName;
        body.data = err;

        try {
            command.setBody(PARSER.writeValueAsString(body));
        } catch (JsonProcessingException e) {
            //TODO do something
            e.printStackTrace();
        }
        callback.success(command);
    }


}