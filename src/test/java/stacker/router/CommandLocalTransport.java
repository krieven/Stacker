package stacker.router;

import stacker.Command;
import stacker.ICallback;

public class CommandLocalTransport implements ICommandTransport {


    @Override
    public void send(String address, Command command, ICallback<Command> callback) {

    }
}
