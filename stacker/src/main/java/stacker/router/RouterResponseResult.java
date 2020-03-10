package stacker.router;

import stacker.Command;
import stacker.SessionStack;

public class RouterResponseResult{
    String sid;
    SessionStack sessionStack;
    Command response;

    RouterResponseResult(String sid, SessionStack sessionStack, Command response){
        this.sid = sid;
        this.sessionStack = sessionStack;
        this.response = response;
    }

    public String getSid() {
        return sid;
    }

    public SessionStack getSessionStack() {
        return sessionStack;
    }

    public Command getResponse() {
        return response;
    }

}