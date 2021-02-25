package stacker.common;

public interface ICallback<Result>{
    void success(Result result);
    void reject(Exception error);
}