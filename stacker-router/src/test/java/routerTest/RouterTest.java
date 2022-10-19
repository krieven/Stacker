package routerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.krieven.stacker.common.config.router.RouterConfig;
import io.github.krieven.stacker.common.config.router.nsRouterConfig.NSRouterConfig;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.krieven.stacker.common.dto.Command;
import io.github.krieven.stacker.common.ICallback;
import io.github.krieven.stacker.router.Router;
import io.github.krieven.stacker.router.SessionStack;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;


public class RouterTest {
    private static final Logger log = LoggerFactory.getLogger(RouterTest.class);
    private static final ObjectMapper PARSER = new ObjectMapper();

    private final MockSessionStorage sessionStorage = new MockSessionStorage();
    private final MockTransport transport = new MockTransport();
    private final Router router = new Router(transport, sessionStorage);
    private final String sid = "111";

    {
        try {
            RouterConfig config = PARSER.readValue(RouterTest.class.getClassLoader().getResourceAsStream("routerConfig.json"), NSRouterConfig.class);
            router.setConfig(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testQuestion() {

        Command respCommand =

                transport.respCommand = new Command() {
                    {
                        setType(Command.Type.QUESTION);
                        setContentBody("Response body".getBytes());
                        setState("entry");
                        setFlow("main");
                        setFlowData("session Data".getBytes());
                    }
                };


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
//////////////////////////////////////
        respCommand.setFlow("subflow");
        respCommand.setType(Command.Type.OPEN);

        transport.nextRespCommand = new Command() {
            {
                setType(Type.QUESTION);
                setFlow("SECOND");
                setState("second_entry");
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
                        assertEquals("main/welcome", sessionStackEntries.peek().getFlow());
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

        transport.respCommand = (
                new Command() {
                    {
                        setType(Type.RETURN);
                        setFlow("SECOND");
                        setState("second_exit");
                        setContentBody("return from SECOND".getBytes());
                    }
                }
        );
        transport.nextRespCommand = new Command() {
            {
                setType(Type.QUESTION);
                setContentBody("MAIN question".getBytes());
                setState("entry");
                setFlowData("session Data ++".getBytes());
            }
        };

        router.handleRequest(sid, "Hello again 1".getBytes(), new Router.IRouterCallback() {

            @Override
            public void success(String sid, String contentType, byte[] body) {
                log.info("success2 " + new String(body));
                assertEquals("MAIN question", new String(body));

                Command lastRequest = transport.lastRequest;
                assertEquals("return from SECOND", new String(lastRequest.getContentBody()));
                assertEquals("RETURN", lastRequest.getType().toString());
                assertEquals("session Data", new String(lastRequest.getFlowData()));

                sessionStorage.find(sid, new ICallback<SessionStack>() {

                    @Override
                    public void success(SessionStack sessionStackEntries) {
                        assertEquals(1, sessionStackEntries.size());
                        byte[] sessionData = sessionStackEntries.peek().getFlowData();
                        assertEquals("main/main", sessionStackEntries.peek().getFlow());
                        assertEquals("session Data ++", new String(sessionData));
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

        transport.respCommand = new Command() {
            {
                setType(Type.ERROR);
                setBodyContentType("text/html");
                setContentBody("bad request".getBytes());
            }
        };

        transport.nextRespCommand = null;

        router.handleRequest(sid, "Hello again 3".getBytes(), new Router.IRouterCallback() {

            @Override
            public void success(String sid, String contentType, byte[] body) {
                log.info("success3 " + new String(body));
                assertEquals("bad request", new String(body));

                Command lastRequest = transport.lastRequest;
                assertEquals("Hello again 3", new String(lastRequest.getContentBody()));
                assertEquals("ANSWER", lastRequest.getType().toString());
                assertEquals("session Data ++", new String(lastRequest.getFlowData()));

                sessionStorage.find(sid, new ICallback<SessionStack>() {

                    @Override
                    public void success(SessionStack sessionStackEntries) {
                        assertEquals(1, sessionStackEntries.size());
                        byte[] sessionData = sessionStackEntries.peek().getFlowData();
                        assertEquals("main/main", sessionStackEntries.peek().getFlow());
                        assertEquals("session Data ++", new String(sessionData));
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

        transport.respCommand = new Command() {
            {
                setBodyContentType("text/html");
                setContentBody("bad request".getBytes());
            }
        };

        transport.nextRespCommand = null;

        router.handleRequest(sid, "Hello again 3".getBytes(), new Router.IRouterCallback() {

            @Override
            public void success(String sid, String contentType, byte[] body) {
                assertNull(body);
            }

            @Override
            public void reject(Exception exception) {
                assertNotNull(exception);
                sessionStorage.find(sid, new ICallback<SessionStack>() {

                    @Override
                    public void success(SessionStack sessionStackEntries) {
                        assertEquals(1, sessionStackEntries.size());
                        byte[] sessionData = sessionStackEntries.peek().getFlowData();
                        assertEquals("main/main", sessionStackEntries.peek().getFlow());
                        assertEquals("session Data ++", new String(sessionData));
                    }

                    @Override
                    public void reject(Exception error) {
                        assertNull(error);
                    }
                });
            }
        });

        transport.respCommand = null;

        router.handleRequest(sid, "Hello again 3".getBytes(), new Router.IRouterCallback() {

            @Override
            public void success(String sid, String contentType, byte[] body) {
                assertNull(sid);
            }

            @Override
            public void reject(Exception exception) {
                assertNotNull(exception);
                assertEquals("null response from flow", exception.getMessage());

                sessionStorage.find(sid, new ICallback<SessionStack>() {

                    @Override
                    public void success(SessionStack sessionStackEntries) {
                        assertEquals(1, sessionStackEntries.size());
                        byte[] sessionData = sessionStackEntries.peek().getFlowData();
                        assertEquals("main/main", sessionStackEntries.peek().getFlow());
                        assertEquals("session Data ++", new String(sessionData));
                    }

                    @Override
                    public void reject(Exception error) {
                        assertNull(error);
                    }
                });
            }
        });


        transport.respCommand = new Command() {
            {
                setFlow("MAIN");
                setState("entry");
                setType(Type.RESOURCE);
                setBodyContentType("text/html");
                setContentBody("ResourceRequest".getBytes());
            }
        };
        Map<String, String[]> parameters = new HashMap<>();

        router.handleResourceRequest(sid, "/path/info", parameters, new Router.IRouterCallback() {

            @Override
            public void success(String sid, String contentType, byte[] body) {
                assertNotNull(body);
                assertEquals("ResourceRequest", new String(body));
            }

            @Override
            public void reject(Exception exception) {

            }
        });
    }

}
