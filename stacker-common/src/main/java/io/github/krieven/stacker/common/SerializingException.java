package io.github.krieven.stacker.common;

public class SerializingException extends Exception {
    public SerializingException(Exception e) {
        super("SerializingException", e);
    }
}
