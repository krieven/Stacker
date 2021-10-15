package stacker.router;


import stacker.common.dto.Command;

class RouterResponseResult {
    private String sid;
    private SessionStack sessionStack;
    private Command response;

    RouterResponseResult(String sid, SessionStack sessionStack, Command response) {
        this.sid = sid;
        this.sessionStack = sessionStack;
        this.response = response;
    }

    String getSid() {
        return sid;
    }

    SessionStack getSessionStack() {
        return sessionStack;
    }

    Command getResponse() {
        return response;
    }

}