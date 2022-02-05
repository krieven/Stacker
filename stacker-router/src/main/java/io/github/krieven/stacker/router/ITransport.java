package io.github.krieven.stacker.router;


import io.github.krieven.stacker.common.dto.Command;
import io.github.krieven.stacker.common.ICallback;

public interface ITransport {
    void sendRequest(String address, Command command, ICallback<Command> callback);
}