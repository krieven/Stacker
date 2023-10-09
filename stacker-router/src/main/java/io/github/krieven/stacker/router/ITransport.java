package io.github.krieven.stacker.router;


import io.github.krieven.stacker.common.dto.Command;
import io.github.krieven.stacker.common.ICallback;

/**
 * Transport interface - the Router uses it to interact with Workflow services
 */
public interface ITransport {
    /**
     * Send command to the Workflow service
     *
     * @param address the address of service
     * @param command the Command to send
     * @param callback the callback to handle response Command
     */
    void sendRequest(String address, Command command, ICallback<Command> callback);
}