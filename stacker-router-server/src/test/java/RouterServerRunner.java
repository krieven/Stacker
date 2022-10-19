import io.github.krieven.stacker.common.JsonParser;
import io.github.krieven.stacker.common.config.router.nsRouterConfig.NSRouterConfig;
import io.github.krieven.stacker.router.server.HttpTransport;
import io.github.krieven.stacker.router.server.RouterServer;
import io.github.krieven.stacker.router.server.SimpleSessionStorage;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class RouterServerRunner {
    public static void main(String[] args) throws Exception {

        RouterServer server = new RouterServer(new HttpTransport(), new SimpleSessionStorage(), 3000);

        InputStream inputStream = RouterServerRunner.class.getClassLoader().getResourceAsStream("router-config-view.json");
        byte[] buf = new byte[1024];
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        while (inputStream != null && inputStream.available() > 0) {
            int len = inputStream.read(buf);
            byteBuffer.write(buf, 0, len);
        }

        io.github.krieven.stacker.common.config.router.RouterConfig config = new JsonParser().parse(byteBuffer.toByteArray(), NSRouterConfig.class);

        if (server.setConfig(config)) {
            server.start();
        }

    }
}
