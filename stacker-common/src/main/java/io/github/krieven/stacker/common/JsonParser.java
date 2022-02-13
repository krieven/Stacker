package io.github.krieven.stacker.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;

public class JsonParser implements IParser {
    private static ObjectMapper PARSER = new ObjectMapper();

    static {
        PARSER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        PARSER.configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false);

        PARSER.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        PARSER.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        PARSER.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        PARSER.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false);
    }

    @Override
    public <T> T parse(byte[] s, Class<T> type) throws ParsingException {
        if (s == null || s.length == 0) return null;
        try {
            return PARSER.readValue(s, type);
        } catch (IOException e) {
            throw new ParsingException(e);
        }
    }

    @Override
    public byte[] serialize(Object o) throws SerializingException {
        if (o == null) return new byte[0];
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
