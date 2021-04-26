import stacker.flow.server.FlowServer;
import testflow.TestFlow;

public class FlowServerRunner {

    public static void main(String[] args) {
        FlowServer flowServer = new FlowServer(new TestFlow(), 3001);
        try {
            flowServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
