package io.github.krieven.stacker.common;

public class ParsingException extends RuntimeException {
    public ParsingException(Exception e) {
        super("ParsingException", e);
    }
}
