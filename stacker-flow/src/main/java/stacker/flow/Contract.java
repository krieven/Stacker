package stacker.flow;

import stacker.common.IParser;

/**
 * @param <A> ArgumentType
 * @param <R> ReturnType
 */

public final class Contract<A, R> {
    private final Class<A> questionType;
    private final Class<R> answerType;
    private final IParser parser;

    public Contract(Class<A> questionType, Class<R> answerType, IParser parser) {
        this.questionType = questionType;
        this.answerType = answerType;
        this.parser = parser;
    }

    public Class<A> getQuestionType() {
        return questionType;
    }

    public Class<R> getAnswerType() {
        return answerType;
    }

    public IParser getParser() {
        return parser;
    }

}
