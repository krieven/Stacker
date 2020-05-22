package stacker.service.main;

public class ServiceMain extends Service<ServiceRq, ServiceRs, StateData, Resources> {

    public ServiceMain(Class<ServiceRq> serviceOpenArgClass, Class<StateData> stateDataClass, Resources resources) {
        super(serviceOpenArgClass, stateDataClass, resources);
    }

    @Override
    public void configure() {

    }

    @Override
    public StateData createStateData() {
        return new StateData();
    }

    @Override
    public ServiceRs makeReturn(RequestContext<StateData, Resources> context) {
        return null;
    }
}
