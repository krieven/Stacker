package io.github.krieven.stacker.common;

public interface IParser {
    <T> T parse(byte[] s, Class<T> type) throws ParsingException;

    byte[] serialize(Object o) throws SerializingException;

    String getContentType();
}
