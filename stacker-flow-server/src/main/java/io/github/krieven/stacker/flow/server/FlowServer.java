package io.github.krieven.stacker.flow.server;

import io.github.krieven.stacker.flow.BaseFlow;
import io.github.krieven.stacker.serve.AsyncServer;

public class FlowServer extends AsyncServer<FlowServlet> {
    public FlowServer(BaseFlow<?, ?, ?> flow, int port) {
        super(new FlowServlet(flow), port);
    }
}
