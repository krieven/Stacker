package stacker.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;

public class JsonParser implements IParser {
    private static ObjectMapper PARSER = new ObjectMapper();

    public JsonParser() {
        PARSER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        PARSER.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);

    }

    @Override
    public <T> T parse(byte[] s, Class<T> type) throws ParsingException {
        if (s == null) return null;
        try {
            return PARSER.readValue(s, type);
        } catch (IOException e) {
            throw new ParsingException(e);
        }
    }

    @Override
    public byte[] serialize(Object o) throws SerializingException {
        try {
            return PARSER.writeValueAsBytes(o);
        } catch (JsonProcessingException e) {
            throw new SerializingException(e);
        }
    }

    @Override
    public String getContentType() {
        return "application/json";
    }

}
