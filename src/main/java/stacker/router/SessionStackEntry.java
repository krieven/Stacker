package stacker.router;

public class SessionStackEntry{
    private String flow;
    private String state;
    private String flowData;
    private String body;
    private String onReturn;

    public String getState() {
        return state;
    }

    public void setState(String name) {
        this.state = name;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getFlowData() {
        return flowData;
    }

    public void setFlowData(String data) {
        this.flowData = data;
    }

    public String getOnReturn() {
        return onReturn;
    }

    public void setOnReturn(String onReturn) {
        this.onReturn = onReturn;
    }

    public String getFlow() {
        return flow;
    }

    public void setFlow(String flow) {
        this.flow = flow;
    }
}