package auth;

import stacker.flow.FlowContext;
import stacker.flow.QuestionState;
import stacker.flow.TheContract;

import java.util.Map;

public class AuthorizingQuestion extends QuestionState<Question, Answer, AuthSupport, Map, AuthorizingQuestion.exits> {

    public enum exits {FORWARD}

    public AuthorizingQuestion() {
        super(
                new TheContract<>(Question.class, Answer.class, null),
                exits.values()
        );
    }

    @Override
    protected void handleAnswer(Answer input, FlowContext<? extends AuthSupport, ? extends Map> context) {
        exit(exits.FORWARD, context);
    }

    @Override
    public void onEnter(FlowContext<? extends AuthSupport, ? extends Map> context) {

    }

}