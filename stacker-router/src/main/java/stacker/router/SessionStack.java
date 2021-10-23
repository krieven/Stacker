package stacker.router;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class SessionStack implements Serializable {

    private static final long serialVersionUID = 1L;
    private final Map<String, byte[]> daemonData = new HashMap<>();
    private Stack<SessionStackEntry> stack = new Stack<>();

    public byte[] getDaemonData(String flowName) {
        flowName = flowName.trim().toUpperCase();
        return daemonData.get(flowName);
    }

    public void setDaemonData(String flowName, byte[] daemonData) {
        flowName = flowName.trim().toUpperCase();
        this.daemonData.put(flowName, daemonData);
    }

    public SessionStackEntry pop() {
        return stack.pop();
    }

    public SessionStackEntry peek() {
        return stack.peek();
    }

    public SessionStackEntry push(SessionStackEntry entry) {
        return stack.push(entry);
    }

    public boolean empty() {
        return stack.empty();
    }

    public int size() {
        return stack.size();
    }
}