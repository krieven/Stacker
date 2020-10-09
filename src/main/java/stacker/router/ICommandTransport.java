package stacker.router;

import stacker.Command;
import stacker.ICallback;

public interface ICommandTransport {
    void sendRequest(String address, Command command, ICallback<Command> callback);
}