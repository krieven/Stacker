package routerTest;

import io.github.krieven.stacker.common.ICallback;
import io.github.krieven.stacker.router.ISessionStorage;
import io.github.krieven.stacker.router.SessionStack;


public class MockSessionStorage implements ISessionStorage {

    public SessionStack sessionStack;

    @Override
    public void find(String id, ICallback<SessionStack> callback) {
        callback.success(sessionStack);
    }

    @Override
    public void save(String id, SessionStack session) {
        sessionStack = session;
    }
}
