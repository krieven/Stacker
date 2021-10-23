import states.auth.AuthAnswer;
import flow.TestFlow;
import org.junit.Test;
import stacker.common.dto.Command;
import stacker.common.ICallback;
import stacker.common.JsonParser;
import stacker.common.SerializingException;
import stacker.flow.BaseFlow;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;

public class RunTest {
    @Test
    public void runTest() {
        BaseFlow flow = new TestFlow();

        Command question1 = new Command();

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
                        question1.setFlowData(command.getFlowData());
                    }

                    @Override
                    public void reject(Exception error) {
                        throw new RuntimeException(error);
                    }
                }
        );

        flow.handleCommand(
                new Command() {
                    {
                        setType(Type.ANSWER);
                        setFlow("main");
                        setState("first");
                        this.setFlowData(question1.getFlowData());
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
                    }

                    @Override
                    public void reject(Exception error) {
                        assertNull(error);
                    }
                }
        );
    }
}
