import stacker.common.JsonParser;
import stacker.router.server.HttpTransport;
import stacker.router.server.RouterServer;
import stacker.router.server.SimpleSessionStorage;
import stacker.common.config.router.RouterConfig;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class RouterServerRunner {
    public static void main(String[] args) throws Exception {

        RouterServer server = new RouterServer(new HttpTransport(), new SimpleSessionStorage(), 3000);

        //do some with config
        InputStream inputStream = RouterServerRunner.class.getClassLoader().getResourceAsStream("router-config-view.json");
        byte[] buf = new byte[1024];
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        while (inputStream.available() > 0) {
            int len = inputStream.read(buf);
            byteBuffer.write(buf, 0, len);
        }


        RouterConfig config = new JsonParser().parse(byteBuffer.toByteArray(), RouterConfig.class);
        server.setConfig(config);

        server.start();

    }
}
