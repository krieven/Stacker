package stacker;

public class SessionStackEntry{
    private String service;
    private String state;
    private String stateData;
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

    public String getStateData() {
        return stateData;
    }

    public void setStateData(String data) {
        this.stateData = data;
    }

    public String getOnReturn() {
        return onReturn;
    }

    public void setOnReturn(String onReturn) {
        this.onReturn = onReturn;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }
}