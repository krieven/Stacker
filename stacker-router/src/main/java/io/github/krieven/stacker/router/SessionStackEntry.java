package io.github.krieven.stacker.router;

import java.io.Serializable;

public class SessionStackEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private String flow;
    private String state;
    private String address;
    private byte[] flowData;

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


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}