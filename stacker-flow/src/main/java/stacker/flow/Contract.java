package stacker.flow;

import stacker.common.IParser;
import stacker.common.ParsingException;
import stacker.common.SerializingException;

/**
 * @param <Q> ArgumentType
 * @param <A> ReturnType
 */

public final class Contract<Q, A> {
    private final Class<Q> questionType;
    private final Class<A> answerType;
    private final IParser parser;

    public Contract(Class<Q> questionType, Class<A> answerType, IParser parser) {
        this.questionType = questionType;
        this.answerType = answerType;
        this.parser = parser;
    }

    public Class<Q> getQuestionType() {
        return questionType;
    }

    public Class<A> getAnswerType() {
        return answerType;
    }

    public IParser getParser() {
        return parser;
    }

    public String getContentType() {
        return getParser().getContentType();
    }

    public byte[] serialize(Q question) throws SerializingException {
        return getParser().serialize(question);
    }

    public A parse(byte[] answer) throws ParsingException {
        return getParser().parse(answer, getAnswerType());
    }
}
