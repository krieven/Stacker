package states.auth;

import org.jetbrains.annotations.NotNull;
import io.github.krieven.stacker.common.JsonParser;
import io.github.krieven.stacker.flow.FlowContext;
import io.github.krieven.stacker.flow.StateQuestion;
import io.github.krieven.stacker.flow.Contract;
import io.github.krieven.stacker.flow.StateCompletion;

public class AuthStateQuestion extends StateQuestion<AuthQuestion, AuthAnswer, AuthSupport, AuthStateQuestion.exits> {

    public AuthStateQuestion() {
        super(
                new Contract<>(
                        AuthQuestion.class,
                        AuthAnswer.class,
                        new JsonParser()
                ),
                exits.values()
        );
        defineResourceController("/hello", new AuthController());
        defineResourceController("/welcome", new AuthController());

    }

    @NotNull
    @Override
    protected StateCompletion handleAnswer(AuthAnswer input, FlowContext<? extends AuthSupport> context) {
        context.getFlowData().setAuthAnswer(input);
        return exitState(exits.FORWARD, context);
    }

    @NotNull
    @Override
    public StateCompletion onEnter(FlowContext<? extends AuthSupport> context) {
        AuthQuestion authQuestion = context.getFlowData().createAuthQuestion();
        authQuestion.setWord("Hello, what is your name?");
        return sendQuestion(authQuestion, context);
    }

    public enum exits {
        FORWARD, BACKWARD
    }

}