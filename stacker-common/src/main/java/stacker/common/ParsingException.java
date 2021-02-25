package stacker.common;

public class ParsingException extends Exception {
    public ParsingException(Exception e) {
        super("ParsingException", e);
    }
}
