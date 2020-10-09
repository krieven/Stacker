package stacker.flow;

import java.util.ArrayList;
import java.util.HashMap;

public class HandlerSends {
    private ArrayList<String> transitions = new ArrayList<>();
    private HashMap<String, Class> externalOpens = new HashMap<>();
    private HashMap<String, String> onReturns = new HashMap<>();
    private boolean isReturns = false;

    public HandlerSends withTransition(String name) {
        transitions.add(name);
        return this;
    }

    public HandlerSends withExternalOpen(String flowName, Class argumentClass, String onReturn) {
        externalOpens.put(flowName, argumentClass);
        onReturns.put(flowName, onReturn);
        return this;
    }

    public HandlerSends withReturn() {
        isReturns = true;
        return this;
    }
}
