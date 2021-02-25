package stacker.router;

public class SessionStackEntry{
    private String flow;
    private String state;
    private String flowData;
    private String contentBody;

    public String getState() {
        return state;
    }

    public void setState(String name) {
        this.state = name;
    }

    public String getContentBody() {
        return contentBody;
    }

    public void setContentBody(String contentBody) {
        this.contentBody = contentBody;
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
}