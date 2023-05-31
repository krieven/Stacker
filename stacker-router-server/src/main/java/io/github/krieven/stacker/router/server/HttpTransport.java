package io.github.krieven.stacker.router.server;

import io.github.krieven.stacker.common.ICallback;
import io.github.krieven.stacker.common.IParser;
import io.github.krieven.stacker.common.JsonParser;
import io.github.krieven.stacker.common.dto.Command;
import io.github.krieven.stacker.router.ITransport;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HttpTransport implements ITransport {

    private final IParser parser = new JsonParser();
    private final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(20))//
            .build();

    @Override
    public void sendRequest(String address, Command command, ICallback<Command> callback) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(address))
                .timeout(Duration.ofMinutes(2))//
                .header("Content-Type", parser.getContentType())
                .POST(HttpRequest.BodyPublishers.ofByteArray(parser.serialize(command)))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApplyAsync(HttpResponse::body)
                .thenAcceptAsync(b -> {
                    try{
                        callback.success(parser.parse(b, Command.class));
                    }
                    catch(Exception e) {
                        callback.reject(e);
                    }
                });
    }
}
