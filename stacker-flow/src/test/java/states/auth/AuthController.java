package states.auth;

import stacker.flow.FlowContext;
import stacker.flow.ResourceController;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

public class AuthController extends ResourceController<AuthSupport> {

    @Override
    protected void handle(List<String> path, Map<String, String> parameters, FlowContext<? extends AuthSupport> context) {
        sendResponse("Hello hello".getBytes(Charset.forName("UTF-8")), context);
    }

    @Override
    protected String getContentType() {
        return null;
    }
}
