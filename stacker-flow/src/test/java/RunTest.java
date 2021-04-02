import flow.Resources;
import flow.TestFlow;
import stacker.common.Command;
import stacker.common.ICallback;
import stacker.flow.BaseFlow;

import static junit.framework.TestCase.assertNotNull;

public class RunTest {
    public static void main(String[] args) {
        BaseFlow flow = new TestFlow(new Resources());
        flow.handleCommand(
                new Command() {
                    {
                        setType(Type.OPEN);
                        setFlow("main");
                        setContentBody("hello".getBytes());
                    }
                },
                new ICallback<Command>() {
                    @Override
                    public void success(Command command) {
                        assertNotNull(command);
                    }

                    @Override
                    public void reject(Exception error) {
                        throw new RuntimeException(error);
                    }
                }
        );
    }
}
