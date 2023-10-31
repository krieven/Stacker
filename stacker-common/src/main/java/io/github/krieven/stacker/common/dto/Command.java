package io.github.krieven.stacker.common.dto;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

public class Command implements Serializable {

    private Type type;
    private String rqUid;
    private String flow;
    private String state;
    private String bodyContentType;
    private byte[] contentBody;
    private byte[] flowData;
    private ResourceRequest resourceRequest;
    private Map<String, String> properties;

    public Type getType() {
        return type;
    }

    public String getRqUid() {
        return rqUid;
    }

    public void setRqUid(String rqUid) {
        this.rqUid = rqUid;
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

    public String getBodyContentType() {
        return bodyContentType;
    }

    public void setBodyContentType(String bodyContentType) {
        this.bodyContentType = bodyContentType;
    }

    public ResourceRequest getResourceRequest() {
        return resourceRequest;
    }

    public void setResourceRequest(ResourceRequest resourceRequest) {
        this.resourceRequest = resourceRequest;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "Command{" +
                "type=" + type +
                ", rqUid='" + rqUid + '\'' +
                ", flow='" + flow + '\'' +
                ", state='" + state + '\'' +
                ", bodyContentType='" + bodyContentType + '\'' +
                ", contentBody=" + Arrays.toString(contentBody) +
                ", flowData=" + Arrays.toString(flowData) +
                ", resourceRequest=" + resourceRequest +
                ", properties=" + properties +
                '}';
    }


    public enum Type {
        QUESTION,
        ANSWER,
        RESOURCE,

        OPEN,
        RETURN,

        ERROR
    }
}