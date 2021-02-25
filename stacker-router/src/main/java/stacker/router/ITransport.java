package stacker.router;


import stacker.common.Command;
import stacker.common.ICallback;

public interface ITransport {
    void sendRequest(String address, Command command, ICallback<Command> callback);
}