package io.github.krieven.stacker.router;


import io.github.krieven.stacker.common.ICallback;

public interface ISessionStorage {
    void find(String id, ICallback<SessionStack> callback);

    void save(String id, SessionStack session);
}