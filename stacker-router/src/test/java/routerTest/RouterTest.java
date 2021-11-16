package routerTest;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stacker.common.dto.Command;
import stacker.common.ICallback;
import stacker.router.Router;
import stacker.router.SessionStack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class RouterTest {
    private static final Logger log = LoggerFactory.getLogger(RouterTest.class);

    @Test
    public void Test() {
        MockTransport transport = new MockTransport();
        MockSessionStorage sessionStorage = new MockSessionStorage();

        Router router = new Router(transport, sessionStorage);

        router.addFlow("main", "http://main.flow");
        router.setMainFlow("main");
        router.addFlow("second", "http://second.flow");
        router.setFlowMapping("main", "subflow", "second");

        ///////////////////////////////////////
        String sid = "111";

        Command respCommand = new Command();
        respCommand.setType(Command.Type.QUESTION);
        respCommand.setContentBody("Response body".getBytes());
        respCommand.setState("entry");
        respCommand.setFlow("main");
        respCommand.setFlowData("session Data".getBytes());

        transport.setRespCommand(respCommand);


        router.handleRequest(sid, "Hello world".getBytes(), new Router.IRouterCallback() {
            @Override
            public void success(String sid, String contentType, byte[] body) {
                log.info("success1 " + new String(body));

                assertEquals("Response body", new String(body));

                Command lastRequest = transport.lastRequest;
                assertEquals("Hello world", new String(lastRequest.getContentBody()));
                assertEquals(Command.Type.OPEN, lastRequest.getType());

                sessionStorage.find(sid, new ICallback<SessionStack>() {

                    @Override
                    public void success(SessionStack sessionStackEntries) {

                        assertEquals(1, sessionStackEntries.size());

                        byte[] sessionData = sessionStackEntries.peek().getFlowData();
                        assertEquals("session Data", new String(sessionData));
                    }

                    @Override
                    public void reject(Exception error) {
                        assertNull(error);
                    }
                });
            }

            @Override
            public void reject(Exception exception) {
                assertNull(exception);
            }
        });

        respCommand.setType(Command.Type.OPEN);
        respCommand.setFlow("subflow");

        transport.nextRespCommand = new Command() {
            {
                setType(Type.QUESTION);
//                setState("first");
//                setFlow("second");
                setContentBody("Second question".getBytes());
                setFlowData("session Data SECOND".getBytes());
            }
        };

        router.handleRequest(sid, "Hello again".getBytes(), new Router.IRouterCallback() {

            @Override
            public void success(String sid, String contentType, byte[] body) {
                log.info("success2 " + new String(body));
                assertEquals("Second question", new String(body));

                Command lastRequest = transport.lastRequest;
                assertEquals("Response body", new String(lastRequest.getContentBody()));
                assertEquals("OPEN", lastRequest.getType().toString());
                assertNull(lastRequest.getFlowData());

                sessionStorage.find(sid, new ICallback<SessionStack>() {

                    @Override
                    public void success(SessionStack sessionStackEntries) {
                        assertEquals(2, sessionStackEntries.size());
                        byte[] sessionData = sessionStackEntries.peek().getFlowData();
                        assertEquals("SECOND", sessionStackEntries.peek().getFlow());
                        assertEquals("session Data SECOND", new String(sessionData));
                    }

                    @Override
                    public void reject(Exception error) {
                        assertNull(error);
                    }
                });
            }

            @Override
            public void reject(Exception exception) {
                assertNull(exception);
            }
        });
    }
}
