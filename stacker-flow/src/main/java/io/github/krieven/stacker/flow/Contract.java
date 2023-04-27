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
public class Contract<Q, A> {
    private final Class<Q> questionType;
    private final Class<A> answerType;
    private final IParser parser;

    /**
     * Constructs the Contract
     *
     * @param questionType - the Question type class
     * @param answerType   - the Answer type class
     * @param parser       - the IParser instance (format)
     */
    public Contract(Class<Q> questionType, Class<A> answerType, IParser parser) {
        this.questionType = questionType;
        this.answerType = answerType;
        this.parser = parser;
    }

    public final Class<Q> getQuestionType() {
        return questionType;
    }

    public final Class<A> getAnswerType() {
        return answerType;
    }

    public final IParser getParser() {
        return parser;
    }

    public final String getContentType() {
        return getParser().getContentType();
    }

    public final byte[] serialize(Q question) throws SerializingException {
        return getParser().serialize(question);
    }

    public final A parse(byte[] answer) throws ParsingException {
        return getParser().parse(answer, getAnswerType());
    }

    public final String getSchema() {

        return null;
    }
}
