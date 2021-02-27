package stacker.common;

import java.io.Serializable;

public class Command implements Serializable {

    private Type type;

    private String flow;
    private String state;

    private byte[] contentBody;

    private byte[] flowData;
    private byte[] daemonData;

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

    public byte[] getContentBody() {
        return contentBody;
    }

    public void setContentBody(byte[] contentBody) {
        this.contentBody = contentBody;
    }

    public byte[] getFlowData() {
        return flowData;
    }

    public void setFlowData(byte[] flowData) {
        this.flowData = flowData;
    }

    public byte[] getDaemonData() {
        return daemonData;
    }

    public void setDaemonData(byte[] daemonData) {
        this.daemonData = daemonData;
    }

}