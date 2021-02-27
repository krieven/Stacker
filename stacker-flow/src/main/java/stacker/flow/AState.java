package stacker.flow;

import org.jetbrains.annotations.NotNull;
import stacker.common.Command;
import stacker.common.SerializingException;

public abstract class AState<FlowDataT, DaemonDataT, ResourcesT> {

    abstract void onEnter(RequestContext<FlowDataT, DaemonDataT, ResourcesT> context);

    abstract void handle(byte[] answer, RequestContext<FlowDataT, DaemonDataT, ResourcesT> context);

    public final void sendTransition(String stateName, @NotNull RequestContext<FlowDataT, DaemonDataT, ResourcesT> context) {
        context.getFlow().sendTransition(stateName, context);
    }

    public final void sendReturn(@NotNull RequestContext<FlowDataT, DaemonDataT, ResourcesT> context) {
        Object returnT = context.getFlow().makeReturn(context);
        Command command = new Command();
        command.setType(Command.Type.RETURN);
        try {
            command.setContentBody(
                    context.getFlow().getContract()
                            .getParser().serialize(returnT)
            );
        } catch (SerializingException e) {
            context.getCallback().reject(e);
        }
        context.getCallback().success(command);
    }
}
