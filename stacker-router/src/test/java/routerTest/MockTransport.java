package routerTest;


import stacker.common.Command;
import stacker.common.ICallback;
import stacker.router.ITransport;

public class MockTransport implements ITransport {

    public Command lastRequest;
    private Command respCommand;

    public void setRespCommand(Command respCommand) {
        this.respCommand = respCommand;
    }

    @Override
    public void sendRequest(String address, Command command, ICallback<Command> callback) {
        lastRequest = command;
        callback.success(respCommand);
    }
}
