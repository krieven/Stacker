package io.github.krieven.stacker.flow.server;

import io.github.krieven.stacker.common.AsyncServer;
import io.github.krieven.stacker.flow.BaseFlow;

public class FlowServer extends AsyncServer<FlowServlet> {
    public FlowServer(BaseFlow<?, ?, ?> flow, int port) {
        super(new FlowServlet(flow), port);
    }
}
