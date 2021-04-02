package flow;

import auth.AuthAnswer;
import auth.AuthSupport;
import auth.AuthQuestion;

public class FlowData implements AuthSupport {
    @Override
    public AuthQuestion createAuthQuestion() {
        return new AuthQuestion();
    }

    private AuthAnswer authAnswer;

    @Override
    public void setAuthAnswer(AuthAnswer answer) {
        this.authAnswer = answer;
    }

    @Override
    public AuthAnswer getAuthAnswer() {
        return authAnswer;
    }
}
