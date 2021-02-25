package stacker.common;

import java.io.Serializable;

public class Command implements Serializable {

    private Type type;

    private String flow;
    private String state;

    private String contentBody;

    private String flowData;
    private String daemonData;

    public enum Type {
        QUESTION,
        ANSWER,

        OPEN,
        RETURN,

        ERROR
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
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

    public String getContentBody() {
        return contentBody;
    }

    public void setContentBody(String contentBody) {
        this.contentBody = contentBody;
    }

    public String getFlowData() {
        return flowData;
    }

    public void setFlowData(String flowData) {
        this.flowData = flowData;
    }

    public String getDaemonData() {
        return daemonData;
    }

    public void setDaemonData(String daemonData) {
        this.daemonData = daemonData;
    }
}