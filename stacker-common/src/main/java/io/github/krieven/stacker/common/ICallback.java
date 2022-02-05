package io.github.krieven.stacker.common;

public interface ICallback<R> {
    void success(R result);

    void reject(Exception error);
}