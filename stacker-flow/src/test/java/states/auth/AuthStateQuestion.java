package states.auth;

import org.jetbrains.annotations.NotNull;
import stacker.common.JsonParser;
import stacker.flow.FlowContext;
import stacker.flow.StateQuestion;
import stacker.flow.Contract;
import stacker.flow.StateCompletion;

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
        defineResourceController("/", new AuthController());
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