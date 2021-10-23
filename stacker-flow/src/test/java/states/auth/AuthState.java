package states.auth;

import stacker.common.JsonParser;
import stacker.flow.FlowContext;
import stacker.flow.QuestionState;
import stacker.flow.Contract;

public class AuthState extends QuestionState<AuthQuestion, AuthAnswer, AuthSupport, AuthState.exits> {

    public AuthState() {
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

    @Override
    protected void handleAnswer(AuthAnswer input, FlowContext<? extends AuthSupport> context) {
        context.getFlowData().setAuthAnswer(input);
        exitState(exits.FORWARD, context);
    }

    @Override
    protected void onBadAnswer(FlowContext<? extends AuthSupport> context) {
        onEnter(context);
    }

    @Override
    public void onEnter(FlowContext<? extends AuthSupport> context) {
        AuthQuestion authQuestion = context.getFlowData().createAuthQuestion();
        authQuestion.setWord("Hello, what is you name?");
        sendQuestion(authQuestion, context);
    }

    public enum exits {
        FORWARD, BACKWARD
    }

}