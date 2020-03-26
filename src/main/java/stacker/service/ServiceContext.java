package stacker.service;

import stacker.Command;
import stacker.ICallback;

public class ServiceContext<StateDataT, ResourcesT> {

    public StateDataT getStateData() {
        return stateData;
    }

    public ResourcesT getResoutces() {
        return resources;
    }

    public String getService() {
        return service;
    }

    public String getState() {
        return state;
    }

    private String service;
    private String state;
    private StateDataT stateData;
    private ResourcesT resources;
    private ICallback<Command> callback;

    ServiceContext(
            String service, String state,
            StateDataT stateData, ResourcesT resources, ICallback<Command> callback
    ) {
        this.service = service;
        this.state = state;
        this.stateData = stateData;
        this.resources = resources;
        this.callback = callback;
    }

    public void sendResult(Object body) {

    }

    public void sendReturn(Object returnT) {

    }

    void sendOpen(Object arg) {

    }

    void sendError(Object err) {

    }

}