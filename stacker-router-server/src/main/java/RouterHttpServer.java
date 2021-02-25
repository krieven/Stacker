import config.RouterConfig;
import org.jetbrains.annotations.NotNull;
import stacker.router.ISessionStorage;
import stacker.router.ITransport;
import stacker.router.Router;

public class RouterHttpServer {

    private Router router;
    private ISessionStorage sessionStorage;
    private ITransport transport;


    public RouterHttpServer withSessionStorage(@NotNull ISessionStorage storage) {
        this.sessionStorage = storage;
        return this;
    }

    public RouterHttpServer withTransport(@NotNull ITransport transport) {
        this.transport = transport;
        return this;
    }


    public void start() {
    }
}
