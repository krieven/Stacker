package states.auth;

import org.jetbrains.annotations.NotNull;
import stacker.flow.FlowContext;
import stacker.flow.ResourceController;
import stacker.flow.StateCompletion;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AuthController extends ResourceController<AuthSupport> {

    @NotNull
    @Override
    protected StateCompletion handle(List<String> path, Map<String, String> parameters, FlowContext<? extends AuthSupport> context) {
//        return sendResponse("Hello hello".getBytes(Charset.forName("UTF-8")), context);
        return new StateCompletion(
                CompletableFuture.supplyAsync(() -> sendResponse("Hello hello".getBytes(Charset.forName("UTF-8")), context))
        );
    }

    @Override
    protected String getContentType() {
        return "text/plain";
    }
}
