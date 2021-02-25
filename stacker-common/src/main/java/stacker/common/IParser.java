package stacker.common;

public interface IParser {
    <T> T parse(String s, Class<T> type) throws ParsingException;

    String serialize(Object o) throws SerializingException;

    String getContentType();
}
