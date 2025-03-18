package io.github.krieven.stacker.router;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class SessionStack implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<String, byte[]> daemonData = new HashMap<>();
    private final Stack<SessionStackEntry> stack = new Stack<>();

    public byte[] getDaemonData(String flowName) {
        return daemonData.get(flowName.trim().toUpperCase());
    }

    public void setDaemonData(String flowName, byte[] daemonData) {
        this.daemonData.put(flowName.trim().toUpperCase(), daemonData);
    }

    public SessionStackEntry pop() {
        if (empty()) {
            return null;
        }
        return stack.pop();
    }

    public SessionStackEntry peek() {
        if (empty()) {
            return null;
        }
        return stack.peek();
    }

    public void push(SessionStackEntry entry) {
        stack.push(entry);
    }

    public boolean empty() {
        return stack.empty();
    }

    public int size() {
        return stack.size();
    }
}