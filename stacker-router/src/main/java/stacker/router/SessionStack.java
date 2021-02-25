package stacker.router;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class SessionStack extends Stack<SessionStackEntry> {

    private static final long serialVersionUID = 1L;

    private Map<String, String> daemonData = new HashMap<>();

    public String getDaemonData(String flowName) {
        flowName = flowName.trim().toUpperCase();
        return daemonData.get(flowName);
    }

    public void setDaemonData(String flowName, String daemonData) {
        flowName = flowName.trim().toUpperCase();
        this.daemonData.put(flowName, daemonData);
    }
}