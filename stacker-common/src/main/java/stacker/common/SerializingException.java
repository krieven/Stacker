package stacker.common;

public class SerializingException extends Exception {
    public SerializingException(Exception e) {
        super("SerializingException", e);
    }
}
