package states.outer;

import org.jetbrains.annotations.NotNull;
import io.github.krieven.stacker.common.JsonParser;
import io.github.krieven.stacker.flow.Contract;
import io.github.krieven.stacker.flow.FlowContext;
import io.github.krieven.stacker.flow.StateOuterCall;
import io.github.krieven.stacker.flow.StateCompletion;

public class OuterCall extends StateOuterCall<OuterQuestion, OuterAnsver, OuterSupport, OuterCall.Exits> {

    public OuterCall() {
        super(new Contract<>(
                OuterQuestion.class,
                OuterAnsver.class,
                new JsonParser()
        ), OuterCall.Exits.values());
    }

    @Override
    protected @NotNull StateCompletion handleAnswer(OuterAnsver answer, FlowContext<? extends OuterSupport> context) {
        return exitState(Exits.SUCCESS, context);
    }

    @Override
    protected @NotNull StateCompletion onErrorParsingAnswer(FlowContext<? extends OuterSupport> context) {
        return exitState(Exits.ERROR, context);
    }

    @Override
    public @NotNull StateCompletion onEnter(FlowContext<? extends OuterSupport> context) {
        return sendQuestion(new OuterQuestion(), context);
    }

    public enum Exits {
        SUCCESS,
        ERROR
    }
}
