package stacker.router.server;

import stacker.common.ICallback;
import stacker.router.ISessionStorage;
import stacker.router.SessionStack;

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
