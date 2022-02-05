package io.github.krieven.stacker.router.server;

import io.github.krieven.stacker.common.ICallback;
import io.github.krieven.stacker.common.IParser;
import io.github.krieven.stacker.common.JsonParser;
import io.github.krieven.stacker.common.SerializingException;
import org.asynchttpclient.*;
import io.github.krieven.stacker.common.dto.Command;
import io.github.krieven.stacker.router.ITransport;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HttpTransport implements ITransport {

    private IParser parser = new JsonParser();

    private AsyncHttpClient client = Dsl.asyncHttpClient();

    private Executor executor = Executors.newCachedThreadPool();

    @Override
    public void sendRequest(String address, Command command, ICallback<Command> callback) {

        try {
            Request request = Dsl.post(address).setRequestTimeout(30000).setBody(parser.serialize(command)).build();

            ListenableFuture<Response> future = client.executeRequest(request);

            future.addListener(() -> {
                try {
                    Response response = future.get();
                    byte[] body = response.getResponseBodyAsBytes();

                    callback.success(parser.parse(body, Command.class));

                } catch (Exception e) {
                    callback.reject(e);
                }

            }, executor);

        } catch (SerializingException e) {
            callback.reject(e);
        }
    }
}
