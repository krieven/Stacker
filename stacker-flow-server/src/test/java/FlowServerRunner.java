import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.krieven.stacker.flow.server.FlowServer;
import testflow.TestFlow;

public class FlowServerRunner {
    private static Logger log = LoggerFactory.getLogger(FlowServerRunner.class);

    public static void main(String[] args) {
        FlowServer flowServer = new FlowServer(new TestFlow(), 3001);
        try {
            flowServer.start();
        } catch (Exception e) {
            log.error("Error starting server", e);
        }
    }

}
