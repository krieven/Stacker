package io.github.krieven.stacker.common;

public class SerializingException extends RuntimeException {
    public SerializingException(Exception e) {
        super("SerializingException", e);
    }
}
