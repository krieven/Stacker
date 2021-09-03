package stacker.flow.server;

import stacker.common.AsyncServer;
import stacker.flow.BaseFlow;

public class FlowServer extends AsyncServer<FlowServlet> {
    public FlowServer(BaseFlow<?, ?, ?> flow, int port) {
        super(new FlowServlet(flow), port);
    }
}
