package stacker.config.server;

import stacker.common.dto.Command;
import stacker.common.ICallback;
import stacker.flow.BaseFlow;
import stacker.router.ITransport;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;

public class NullTransport implements ITransport {

    private Map<String, BaseFlow> addressMap = new HashMap<>();

    @Override
    public void sendRequest(String address, Command command, ICallback<Command> callback) {
        if (!addressMap.containsKey(address)) {
            callback.reject(new MissingResourceException("Flow not found", BaseFlow.class.getCanonicalName(), "address"));
        }
        addressMap.get(address).handleCommand(command, callback);
    }

    public void addFlow(String address, BaseFlow flow) {

        addressMap.put(address, flow);
    }
}
