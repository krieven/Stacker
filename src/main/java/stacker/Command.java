package stacker;

public class Command{
    private RouterCommand command;
    private String service;
    private String state;
    private String stateData;
    private String body;
    private String onReturn;

    public RouterCommand getCommand() {
        return command;
    }

    public void setCommand(RouterCommand type) {
        this.command = type;
    }

    public String getOnReturn() {
        return onReturn;
    }

    public void setOnReturn(String onReturn) {
        this.onReturn = onReturn;
    }

    public String getStateData() {
        return stateData;
    }

    public void setStateData(String data) {
        this.stateData = data;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
    
}