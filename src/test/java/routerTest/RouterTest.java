package routerTest;

import org.junit.Test;
import stacker.Command;
import stacker.ICallback;
import stacker.router.ICommandTransport;
import stacker.router.ISessionStorage;
import stacker.router.Router;
import stacker.router.SessionStack;

public class RouterTest {

    public Router createRouter() {
        return new Router(
                (String address, Command command, ICallback<Command> callback) -> {
                    callback.success(new Command());
                },
                new ISessionStorage() {
                    @Override
                    public void find(String id, ICallback<SessionStack> callback) {
                        callback.success(null);
                    }

                    @Override
                    public void save(String id, SessionStack session) {

                    }
                }
        );
    }

    @Test
    public void addFlowTest() {
        Router router = createRouter();
        router.addFlow("main", "http://localhost:300");
        router.setDefaultFlow("main");
        router.addFlow("login", "http://localhost:3001");
        router.validate();

        router.handle("1", "{}", new Router.IRouterCallback() {
            @Override
            public void success(String sid, String body) {
                System.out.println(sid);
                System.out.println(body);
            }

            @Override
            public void reject(Exception exception) {

            }
        });
    }
}
