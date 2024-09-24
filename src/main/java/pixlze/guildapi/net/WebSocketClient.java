package pixlze.guildapi.net;

import pixlze.guildapi.net.type.Api;

import java.net.URI;
import java.net.http.WebSocket;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WebSocketClient extends Api {
    private WebSocket.Listener listener;
    // connect to api

    protected WebSocketClient() {
        super("websocket", List.of(GuildApiClient.class));
        CompletableFuture<WebSocket> ws = NetManager.HTTP_CLIENT.newWebSocketBuilder()
                .buildAsync(URI.create("null"), listener);
    }
}
