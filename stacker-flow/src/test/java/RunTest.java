import io.github.krieven.stacker.common.dto.ResourceRequest;
import io.github.krieven.stacker.flow.FlowHolder;
import states.auth.AuthAnswer;
import flow.TestFlow;
import org.junit.Test;
import io.github.krieven.stacker.common.dto.Command;
import io.github.krieven.stacker.common.ICallback;
import io.github.krieven.stacker.common.JsonParser;
import io.github.krieven.stacker.common.SerializingException;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;

public class RunTest {
    private FlowHolder flow;

    public void createFlow(){
        flow = FlowHolder.create(new TestFlow());
    }
    @Test
    public void testOpen() {
        createFlow();
        flow.handleCommand(
                new Command() {
                    {
                        setType(Type.OPEN);
                        setFlow("main");
                        try {
                            setContentBody(new JsonParser().serialize("hello"));
                        } catch (SerializingException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new ICallback<Command>() {
                    @Override
                    public void success(Command command) {
                        assertNotNull(command);
                        assertEquals(Command.Type.QUESTION, command.getType());
                        assertEquals("FIRST", command.getState());
                        assertEquals(
                                "{\"argument\":\"hello\",\"authAnswer\":null}",
                                new String(command.getFlowData())
                        );
                        assertEquals(
                                "{\"word\":\"Hello, what is your name?\"}",
                                new String(command.getContentBody())
                        );
                    }

                    @Override
                    public void reject(Exception error) {
                        assertNull(error);
                    }
                }
        );
    }

    @Test
    public void testAnswer() {
        createFlow();
        flow.handleCommand(
                new Command() {
                    {
                        setType(Type.ANSWER);
                        setFlow("main");
                        setState("first");
                        this.setFlowData("{\"argument\":\"hello\"}".getBytes());
                        try {
                            this.setContentBody(new JsonParser().serialize(
                                    new AuthAnswer() {
                                        {
                                            setName("John Smith");
                                        }
                                    })
                            );
                        } catch (SerializingException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new ICallback<Command>() {
                    @Override
                    public void success(Command command) {
                        assertNotNull(command);
                        assertEquals("OUTERCALL", command.getFlow());
                        assertEquals("OUTERCALL", command.getState());
                        assertEquals("{}", new String(command.getContentBody()));
                        assertEquals("{\"argument\":\"hello\",\"authAnswer\":{\"name\":\"John Smith\"}}", new String(command.getFlowData()));
                    }

                    @Override
                    public void reject(Exception error) {
                        assertNull(error);
                    }
                }
        );
    }

    @Test
    public void testReturn() {
        createFlow();
        flow.handleCommand(
                new Command() {
                    {
                        setType(Type.RETURN);
                        setFlow("main");
                        setState("outerCall");
                        setFlowData("{\"authAnswer\":{\"name\":\"John Smith\"}}".getBytes());
                        setContentBody("{}".getBytes());
                    }
                },
                new ICallback<Command>() {
                    @Override
                    public void success(Command command) {
                        assertNotNull(command);
                        assertEquals(Command.Type.RETURN, command.getType());
                        assertEquals("\"John Smith\"", new String(command.getContentBody()));
                    }

                    @Override
                    public void reject(Exception error) {
                        assertNull(error);
                    }
                });
    }

    @Test
    public void testResource() {
        createFlow();
        flow.handleCommand(
                new Command() {
                    {
                        setType(Type.RESOURCE);
                        setFlow("main");
                        setState("first");
                        setFlowData("{\"authAnswer\":{\"name\":\"John Smith\"}}".getBytes());
                        setResourceRequest(
                                new ResourceRequest() {
                                    {
                                        setPath("/hello/world");
                                    }
                                }
                        );
                    }
                },
                new ICallback<Command>() {
                    @Override
                    public void success(Command command) {
                        assertNotNull(command);
                        assertEquals(Command.Type.RESOURCE, command.getType());
                        assertEquals("Hello hello", new String(command.getContentBody()));
                        assertNull(command.getFlowData());
                        assertEquals("text/plain", command.getBodyContentType());
                    }

                    @Override
                    public void reject(Exception error) {
                        assertNull(error);
                    }
                }
        );

        flow.handleCommand(
                new Command() {
                    {
                        setType(Type.RESOURCE);
                        setFlow("main");
                        setState("first");
                        setFlowData("{\"authAnswer\":{\"name\":\"John Smith\"}}".getBytes());
                        setResourceRequest(
                                new ResourceRequest() {
                                    {
                                        setPath("/world");
                                    }
                                }
                        );
                    }
                },
                new ICallback<Command>() {
                    @Override
                    public void success(Command command) {
                        assertNotNull(command);
                        assertEquals(Command.Type.RESOURCE, command.getType());
                        assertEquals("404 Resource not found", new String(command.getContentBody()));
                        assertNull(command.getFlowData());
                        assertEquals("text/html", command.getBodyContentType());
                    }

                    @Override
                    public void reject(Exception error) {
                        assertNull(error);
                    }
                }
        );
    }
}
