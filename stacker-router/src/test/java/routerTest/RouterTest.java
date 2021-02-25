package routerTest;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stacker.common.Command;
import stacker.common.ICallback;
import stacker.router.Router;
import stacker.router.SessionStack;

import static org.junit.Assert.assertEquals;


public class RouterTest {
    private static Logger log = LoggerFactory.getLogger(RouterTest.class);

    @Test
    public void Test() {
        MockTransport transport = new MockTransport();
        MockSessionStorage sessionStorage = new MockSessionStorage();

        Router router = new Router(transport, sessionStorage);

        router.addFlow("main", "http://main.flow");
        router.setMainFlow("main");

        ///////////////////////////////////////
        String sid = "111";

        Command respCommand = new Command();
        respCommand.setType(Command.Type.QUESTION);
        respCommand.setContentBody("Response body");
        respCommand.setState("entry");
        respCommand.setFlow("main");
        respCommand.setFlowData("session Data");

        transport.setRespCommand(respCommand);


        router.handleRequest(sid, "Hello world", new Router.IRouterCallback() {
            @Override
            public void success(String sid, String body) {
                assertEquals("Response body", body);
                Command lastRequest = transport.lastRequest;
                assertEquals("Hello world", lastRequest.getContentBody());
                assertEquals("OPEN", lastRequest.getType().toString());
                sessionStorage.find(sid, new ICallback<SessionStack>() {

                    @Override
                    public void success(SessionStack sessionStackEntries) {
                        assertEquals(1, sessionStackEntries.size());
                        String sessiondata = sessionStackEntries.peek().getFlowData();
                        assertEquals("session Data", sessiondata);
                    }

                    @Override
                    public void reject(Exception error) {

                    }
                });
            }

            @Override
            public void reject(Exception exception) {

            }
        });

        router.handleRequest(sid, "Hello again", new Router.IRouterCallback() {

            @Override
            public void success(String sid, String body) {
                Command lastRequest = transport.lastRequest;
                assertEquals("Hello again", lastRequest.getContentBody());
                assertEquals("ANSWER", lastRequest.getType().toString());
                assertEquals("session Data", lastRequest.getFlowData());
                sessionStorage.find(sid, new ICallback<SessionStack>() {

                    @Override
                    public void success(SessionStack sessionStackEntries) {
                        assertEquals(1, sessionStackEntries.size());
                        String sessiondata = sessionStackEntries.peek().getFlowData();
                        assertEquals("session Data", sessiondata);
                    }

                    @Override
                    public void reject(Exception error) {

                    }
                });
            }

            @Override
            public void reject(Exception exception) {

            }
        });
    }
}
