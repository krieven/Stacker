package io.github.krieven.stacker.flow;

import io.github.krieven.stacker.common.ICallback;
import io.github.krieven.stacker.common.dto.Command;
import org.jetbrains.annotations.NotNull;

/**
 * The Flow holder, should be used either by the server and for test purpose
 */
public final class FlowHolder {

    private final BaseFlow<?, ?, ?> flow;

    private FlowHolder(BaseFlow<?, ?, ?> flow) {
        this.flow = flow;
        flow.configureAndVerify();
    }

    public static FlowHolder create(BaseFlow<?,?,?> flow){
        if(flow == null){
            throw new IllegalArgumentException("the flow cannot be null");
        }
        return new FlowHolder(flow);
    }

    /**
     * This method should be called by server to handle command
     *
     * @param command  - the Command that server receive
     * @param callback - the server callback
     */
    public void handleCommand(@NotNull Command command, @NotNull ICallback<Command> callback){
        flow.handleCommand(command, callback);
    }

    /**
     * Flow provided
     * @return the Flow
     */
    public BaseFlow<?, ?, ?> getFlow() {
        return flow;
    }
}
