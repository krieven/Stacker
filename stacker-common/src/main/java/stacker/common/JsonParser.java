package stacker.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonParser implements IParser {
    private static ObjectMapper PARSER = new ObjectMapper();

    @Override
    public <T> T parse(String s, Class<T> type) throws ParsingException {
        try {
            return PARSER.readValue(s, type);
        } catch (IOException e) {
            throw new ParsingException(e);
        }
    }

    @Override
    public String serialize(Object o) throws SerializingException {
        try {
            return PARSER.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new SerializingException(e);
        }
    }

    @Override
    public String getContentType() {
        return "application/json";
    }

}
