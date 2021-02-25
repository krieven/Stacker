package stacker.router;


import stacker.common.ICallback;

public interface ISessionStorage {
    void find(String id, ICallback<SessionStack> callback);
    void save(String id, SessionStack session);
}