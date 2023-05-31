package states.auth;

import org.jetbrains.annotations.NotNull;
import io.github.krieven.stacker.flow.FlowContext;
import io.github.krieven.stacker.flow.ResourceController;
import io.github.krieven.stacker.flow.StateCompletion;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AuthController extends ResourceController<AuthSupport> {

    @NotNull
    @Override
    protected StateCompletion handle(List<String> path, Map<String, String[]> parameters, FlowContext<? extends AuthSupport> context) {
//        return sendResponse("Hello hello".getBytes(Charset.forName("UTF-8")), context);

        return new StateCompletion(
                CompletableFuture.supplyAsync(
                        () -> sendResponse("Hello hello".getBytes(StandardCharsets.UTF_8), context)
                )
        );
    }

    @Override
    protected String getContentType() {
        return "text/plain";
    }
}
