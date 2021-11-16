package routerTest;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stacker.common.dto.Command;
import stacker.common.ICallback;
import stacker.router.ITransport;

public class MockTransport implements ITransport {
    private static final Logger log = LoggerFactory.getLogger(MockTransport.class);

    public Command lastRequest;
    private Command respCommand;
    public Command nextRespCommand;

    public void setRespCommand(Command respCommand) {
        this.respCommand = respCommand;
    }

    @Override
    public void sendRequest(String address, Command command, ICallback<Command> callback) {
        lastRequest = command;
        log.info("------->");
        log.info(lastRequest.toString());
        log.info(respCommand.toString());
        log.info("<-------");

        Command currenrResp = respCommand;
        if (nextRespCommand != null) respCommand = nextRespCommand;

        callback.success(currenrResp);
    }
}
