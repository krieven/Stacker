import org.junit.Ignore;
import stacker.common.dto.ResourceRequest;
import states.auth.AuthAnswer;
import flow.TestFlow;
import org.junit.Test;
import stacker.common.dto.Command;
import stacker.common.ICallback;
import stacker.common.JsonParser;
import stacker.common.SerializingException;
import stacker.flow.BaseFlow;

import java.util.HashMap;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;

public class RunTest {
    private BaseFlow flow = new TestFlow();

    @Test
    public void testOpen() {
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
                    }

                    @Override
                    public void reject(Exception error) {
                        assertNull(error);
                    }
                }
        );
    }
}
