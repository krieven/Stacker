package auth;

import stacker.common.JsonParser;
import stacker.flow.FlowContext;
import stacker.flow.QuestionState;
import stacker.flow.Contract;

public class AuthState extends QuestionState<AuthQuestion, AuthAnswer, AuthSupport, AuthResources, AuthState.exits> {

    public enum exits {
        FORWARD, BACKWARD
    }

    public AuthState() {
        super(
                new Contract<>(
                        AuthQuestion.class,
                        AuthAnswer.class,
                        new JsonParser()
                ),
                exits.values()
        );
    }

    @Override
    protected void handleAnswer(AuthAnswer input, FlowContext<? extends AuthSupport, ? extends AuthResources> context) {
        context.getFlowData().setAuthAnswer(input);
        exitState(exits.FORWARD, context);
    }

    @Override
    public void onEnter(FlowContext<? extends AuthSupport, ? extends AuthResources> context) {
        AuthQuestion authQuestion = context.getFlowData().createAuthQuestion();
        authQuestion.setWord("Hello, what is you name?");
        sendQuestion(authQuestion, context);
    }

}