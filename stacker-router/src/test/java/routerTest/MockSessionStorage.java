package routerTest;

import stacker.common.ICallback;
import stacker.router.ISessionStorage;
import stacker.router.SessionStack;


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
