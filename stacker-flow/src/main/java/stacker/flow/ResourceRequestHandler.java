package stacker.flow;

import java.util.Map;


public abstract class ResourceRequestHandler<F> {

    abstract void handleResourceRequest(String pathInfo, Map<String, String[]> parameters, FlowContext<? super F> context);

    abstract void getContentType();
}
