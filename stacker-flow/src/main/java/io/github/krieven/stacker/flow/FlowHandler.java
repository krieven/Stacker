package io.github.krieven.stacker.flow;

import io.github.krieven.stacker.common.ICallback;
import io.github.krieven.stacker.common.dto.Command;
import org.jetbrains.annotations.NotNull;

public class FlowHandler {

    private final BaseFlow<?, ?, ?> flow;

    private FlowHandler(BaseFlow<?, ?, ?> flow) {
        this.flow = flow;
        flow.configureAndVerify();
    }

    public static FlowHandler create(BaseFlow<?,?,?> flow){
        if(flow == null){
            throw new IllegalArgumentException("the flow cannot be null");
        }
        return new FlowHandler(flow);
    }

    public final void handleCommand(@NotNull Command command, @NotNull ICallback<Command> callback){
        flow.handleCommand(command, callback);
    }

    public BaseFlow<?, ?, ?> getFlow() {
        return flow;
    }
}
