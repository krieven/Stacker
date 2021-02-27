package stacker.router;

public class SessionStackEntry{
    private String flow;
    private String state;
    private byte[] flowData;
    private byte[] contentBody;

    public String getState() {
        return state;
    }

    public void setState(String name) {
        this.state = name;
    }

    public String getFlow() {
        return flow;
    }

    public void setFlow(String flow) {
        this.flow = flow;
    }


    public byte[] getFlowData() {
        return flowData;
    }

    public void setFlowData(byte[] flowData) {
        this.flowData = flowData;
    }

    public byte[] getContentBody() {
        return contentBody;
    }

    public void setContentBody(byte[] contentBody) {
        this.contentBody = contentBody;
    }
}