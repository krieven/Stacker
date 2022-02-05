package io.github.krieven.stacker.router.server;

import io.github.krieven.stacker.common.ICallback;
import io.github.krieven.stacker.router.ISessionStorage;
import io.github.krieven.stacker.router.SessionStack;

import java.util.HashMap;
import java.util.Map;

public class SimpleSessionStorage implements ISessionStorage {

    private Map<String, SessionStack> storage = new HashMap<>();

    @Override
    public void find(String id, ICallback<SessionStack> callback) {
        callback.success(storage.get(id));
    }

    @Override
    public void save(String id, SessionStack session) {
        storage.put(id, session);
    }

}
