package flow;

import states.auth.AuthAnswer;
import states.auth.AuthSupport;
import states.auth.AuthQuestion;

public class FlowData implements AuthSupport {

    private String argument;
    private AuthAnswer authAnswer;

    public FlowData() {
    }

    FlowData(String argument) {
        this.argument = argument;
    }

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

    public String getArgument() {
        return argument;
    }

    public void setArgument(String argument) {
        this.argument = argument;
    }

}
