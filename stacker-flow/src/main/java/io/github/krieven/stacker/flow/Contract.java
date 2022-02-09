package io.github.krieven.stacker.flow;

import io.github.krieven.stacker.common.IParser;
import io.github.krieven.stacker.common.ParsingException;
import io.github.krieven.stacker.common.SerializingException;

/**
 * The instance of this class represents interaction Contract between State and opening Workflow
 * or between State and Client.
 * Contract is immutable
 *
 * @param <Q> ArgumentType
 * @param <A> ReturnType
 */
public final class Contract<Q, A> {
    private final Class<Q> questionType;
    private final Class<A> answerType;
    private final IParser parser;

    /**
     * Constructs the Contract
     *
     * @param questionType - the Question type class
     * @param answerType   - the Answer type class
     * @param parser       -
     */
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

    public String getSchema() {

        return null;
    }
}
