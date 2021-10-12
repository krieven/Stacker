package flow;

import auth.AuthAnswer;
import auth.AuthSupport;
import auth.AuthQuestion;

public class FlowData implements AuthSupport {
    private AuthAnswer authAnswer;

    @Override
    public AuthQuestion createAuthQuestion() {
        return new AuthQuestion();
    }

    @Override
    public AuthAnswer getAuthAnswer() {
        return authAnswer;
    }

    @Override
    public void setAuthAnswer(AuthAnswer answer) {
        this.authAnswer = answer;
    }
}
