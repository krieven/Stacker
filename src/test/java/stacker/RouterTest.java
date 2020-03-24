package stacker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import stacker.Command;
import stacker.ICallback;
import stacker.router.ICommandTransport;
import stacker.Command.Type;
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
                assertEquals(Command.Type.OPEN, command.getCommand());
                assertEquals("main", command.getService());
                assertEquals("body", command.getBody());
                assertEquals("address", address);


                Command responseCommand = new Command();
                responseCommand.setCommand(Command.Type.RESULT);
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

    @Test
    public void testSerialize(){
        ObjectMapper parser = new ObjectMapper();
        try {
            String res = parser.writeValueAsString(new ICallback<Command>() {

                public String value ="Hello value";
                public String name = "Hello name";

                @Override
                public void success(Command command) {

                }

                @Override
                public void reject(Exception error) {

                }
            });
            String res1 = parser.writeValueAsString(new Command());
            System.out.println(res1);
            System.out.println(res);
            assertEquals("{\"value\":\"Hello value\",\"name\":\"Hello name\"}", res);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
