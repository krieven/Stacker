package stacker.flow;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseState<FlowDataI, ResourcesI, ExitsE extends Enum<ExitsE>> {

    private Map<ExitsE, String> transitions = new HashMap<>();

    private Enum<ExitsE>[] exits;

    public BaseState(Enum<ExitsE>[] exits) {
        this.exits = exits;
    }

    public BaseState<FlowDataI, ResourcesI, ExitsE> withExit(ExitsE name, String target) {
        target = target.trim().toUpperCase();
        if (transitions.containsKey(name))
            throw new RuntimeException("transition " + name + " already defined");
        transitions.put(name, target);
        return this;
    }

    Enum<ExitsE>[] getExits() {
        return exits;
    }

    private String getTransition(ExitsE key) {
        return transitions.get(key);
    }

    public abstract void onEnter(FlowContext<? extends FlowDataI, ? extends ResourcesI> context);

    abstract void handle(byte[] answer, FlowContext<? extends FlowDataI, ? extends ResourcesI> context);

    public final void exit(ExitsE target, FlowContext<? extends FlowDataI, ? extends ResourcesI> context) {
        context.sendTransition(getTransition(target));
    }

    public final void sendReturn(FlowContext<? extends FlowDataI, ? extends ResourcesI> context) {
        context.sendReturn();
    }
}
