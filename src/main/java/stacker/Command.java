package stacker;

public class Command{
    private Type command;
    private String flow;
    private String state;
    private String flowData;
    private String body;
    private String onReturn;

    public enum Type {
        ACTION,
        OPEN,
        RETURN,
        RESULT,
        TRANSITION,
        ERROR
    }

    public Type getCommand() {
        return command;
    }

    public void setCommand(Type type) {
        this.command = type;
    }

    public String getOnReturn() {
        return onReturn;
    }

    public void setOnReturn(String onReturn) {
        this.onReturn = onReturn;
    }

    public String getFlowData() {
        return flowData;
    }

    public void setFlowData(String data) {
        this.flowData = data;
    }

    public String getFlow() {
        return flow;
    }

    public void setFlow(String flow) {
        this.flow = flow;
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