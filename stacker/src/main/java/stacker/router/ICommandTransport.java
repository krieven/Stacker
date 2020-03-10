package stacker.router;

import stacker.Command;
import stacker.ICallback;

public interface ICommandTransport {
    void send(String address, Command command, ICallback<Command> callback);
}