package states.auth;

import javax.validation.constraints.NotNull;
import io.github.krieven.stacker.common.JsonParser;
import io.github.krieven.stacker.flow.FlowContext;
import io.github.krieven.stacker.flow.StateQuestion;
import io.github.krieven.stacker.flow.Contract;
import io.github.krieven.stacker.flow.StateCompletion;

public class AuthStateQuestion extends StateQuestion<AuthQuestion, AuthAnswer, AuthSupport, AuthStateQuestion.Exits> {

    public AuthStateQuestion() {
        super(
                new Contract<>(
                        AuthQuestion.class,
                        AuthAnswer.class,
                        new JsonParser()
                ),
                Exits.values()
        );
        defineResourceController("/hello", new AuthController());
        defineResourceController("/welcome", new AuthController());

    }

    @NotNull
    @Override
    protected StateCompletion handleAnswer(AuthAnswer input, FlowContext<? extends AuthSupport> context) {
        context.getFlowData().setAuthAnswer(input);
        return exitState(Exits.FORWARD, context);
    }

    @NotNull
    @Override
    public StateCompletion onEnter(FlowContext<? extends AuthSupport> context) {
        AuthQuestion authQuestion = context.getFlowData().createAuthQuestion();
        authQuestion.setWord("Hello, what is your name?");
        return sendQuestion(authQuestion, context);
    }

    public enum Exits {
        FORWARD, BACKWARD
    }

}