import stacker.common.JsonParser;
import stacker.router.server.HttpTransport;
import stacker.router.server.RouterServer;
import stacker.router.server.SimpleSessionStorage;
import stacker.router.server.config.RouterConfig;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class RouterServerRunner {
    public static void main(String[] args) throws Exception {

        RouterServer server = new RouterServer(new HttpTransport(), new SimpleSessionStorage(), 3000);

        //do some with config
        InputStream inputStream = RouterServerRunner.class.getClassLoader().getResourceAsStream("router-config-view.json");
        byte[] buf = new byte[1024];
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        while (inputStream.available() > 0) {
            int len = inputStream.read(buf);
            byteArray.write(buf, 0, len);
        }

        server.start();

        RouterConfig config = new JsonParser().parse(byteArray.toByteArray(), RouterConfig.class);
        server.setConfig(config);

        byte[] b = new byte[1024];
        int len = System.in.read(b);

        String userInput = new String(b, 0, len);

        if ("stop".equals(userInput.trim())) {
            server.stop();
            System.exit(0);
        }
    }
}
