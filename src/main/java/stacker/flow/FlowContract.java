package stacker.flow;

public class FlowContract {
    private Class openArgTClass;
    private Class returnTClass;

    public FlowContract(Class openArgTClass, Class returnTClass) {
        this.setOpenArgTClass(openArgTClass);
        this.setReturnTClass(returnTClass);
    }

    public Class getOpenArgTClass() {
        return openArgTClass;
    }

    public void setOpenArgTClass(Class openArgTClass) {
        this.openArgTClass = openArgTClass;
    }

    public Class getReturnTClass() {
        return returnTClass;
    }

    public void setReturnTClass(Class returnTClass) {
        this.returnTClass = returnTClass;
    }
}
