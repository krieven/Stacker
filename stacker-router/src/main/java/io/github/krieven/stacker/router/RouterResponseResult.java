package io.github.krieven.stacker.router;


import io.github.krieven.stacker.common.dto.Command;

import javax.validation.constraints.NotNull;

class RouterResponseResult {
    private final String sid;
    private final SessionStack sessionStack;
    private final Command response;

    RouterResponseResult(@NotNull String sid, @NotNull SessionStack sessionStack, @NotNull Command response) {
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