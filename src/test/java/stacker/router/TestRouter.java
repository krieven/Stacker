package stacker.router;

import org.junit.Test;

public class TestRouter {
    private Router router;

    public void createRouter() {
        router = new Router(new CommandLocalTransport(), new SimpleSessionStorage());
        router.addService("main", "mainService");
        router.setDefaultService("main");


        router.validate();
    }

    @Test
    public void testRouter() {
        createRouter();
//        router.handle();
    }


}
