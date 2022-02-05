package routerTest;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.krieven.stacker.common.dto.Command;
import io.github.krieven.stacker.common.ICallback;
import io.github.krieven.stacker.router.ITransport;

public class MockTransport implements ITransport {
    private static final Logger log = LoggerFactory.getLogger(MockTransport.class);

    Command lastRequest;
    Command respCommand;
    Command nextRespCommand;


    @Override
    public void sendRequest(String address, Command command, ICallback<Command> callback) {
        lastRequest = command;
        try {
            log.info("------->");
            log.info(lastRequest.toString());
            log.info(respCommand.toString());
            log.info("<-------");
        } catch (Exception e) {
            log.info("request or response command are null");
        }
        Command currentResp = respCommand;
        if (nextRespCommand != null) respCommand = nextRespCommand;

        callback.success(currentResp);
    }
}
