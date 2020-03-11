package stacker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import stacker.Command;
import stacker.ICallback;
import stacker.router.ICommandTransport;
import stacker.RouterCommand;
import stacker.SessionStack;
import stacker.router.ISessionStorage;
import stacker.router.Router;
import stacker.router.Router.IRouterCallback;

/**
 * Unit test for simple App.
 */
public class RouterTest 
{
    /**
     */
    @Test
    public void testRouterHandleNewSession(){
        ICommandTransport transport = new ICommandTransport() {
        
            @Override
            public void send(String address, Command command, ICallback<Command> callback) {
                assertEquals(RouterCommand.OPEN, command.getCommand());
                assertEquals("main", command.getService());
                assertEquals("body", command.getBody());
                assertEquals("address", address);


                Command responseCommand = new Command();
                responseCommand.setCommand(RouterCommand.RESULT);
                responseCommand.setState("state0");
                responseCommand.setStateData("data");
                responseCommand.setBody("body1");

                assertNotNull("Callback is null", callback);

                callback.success(responseCommand);
            }
        };
        ISessionStorage newSession = new ISessionStorage(){
        
            @Override
            public void save(String id, SessionStack session) {}
        
            @Override
            public void find(String id, ICallback<SessionStack> callback) {
                callback.success(null);
            }
        };
        Router router = new Router(transport, newSession);

        router.addService("main", "address");
        router.setDefaultService("main");

        router.validate();

        router.handle("sid", "body", new IRouterCallback(){

            @Override
            public void success(String sid, String body) {
                assertEquals("sid", sid);
                assertEquals("body1", body);
                System.out.println("Yes, all right");
            }

            @Override
            public void reject(Exception exception) {

            }

        });

    }
}
