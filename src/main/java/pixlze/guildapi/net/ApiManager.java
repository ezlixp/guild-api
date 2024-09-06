package pixlze.guildapi.net;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.MinecraftClient;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.mod.event.WynncraftConnectionEvents;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiManager {
    // dependency : connection manager
    public static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    public JsonObject wynnPlayerInfo;
    public GuildServerManager Guild = new GuildServerManager();


    private void onWynnJoin(MinecraftClient client) {
        if (client.player != null && wynnPlayerInfo == null) {
            new Thread(() -> {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
//                            .uri(URI.create("https://api.wynncraft.com/v3/player/" + "pixlze"))
                            .uri(URI.create("https://api.wynncraft.com/v3/player/" + client.player.getUuidAsString()))
                            .build();
                    HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                    GuildApi.LOGGER.info("wynn response: {}", response.body());
                    wynnPlayerInfo = GuildApi.gson.fromJson(response.body(), JsonObject.class);
                    if (wynnPlayerInfo.get("Error") != null) {
                        String message = wynnPlayerInfo.get("Error").getAsString();
                        wynnPlayerInfo = null;
                        Guild.crash();
                        throw new Exception(message);
                    }
                    try {
                        Guild.BaseURL = GuildApi.secrets.get("guild_raid_urls").getAsJsonObject().get(wynnPlayerInfo.get("guild").getAsJsonObject().get("prefix").getAsString()).getAsString();
                    } catch (Exception e) {
                        Guild.crash();
                        GuildApi.LOGGER.error("couldn't load guild server base url of guild: {}", wynnPlayerInfo.get("guild"));
                    }
                    GuildApi.LOGGER.info("successfully loaded wynn player info");
                } catch (Exception e) {
                    GuildApi.LOGGER.error("wynn player load error: {} {}", e, e.getMessage());
                    Guild.crash();
                }
            }).start();
        } else {
            GuildApi.LOGGER.warn("wynn player already initialized or null player");
        }
    }

    public void init() {
        WynncraftConnectionEvents.JOIN.register(this::onWynnJoin);
    }

    public class GuildServerManager {
        protected String BaseURL;
        private boolean enabled = true;
        private String token; // put in cache

        public void crash() {
            GuildApi.LOGGER.warn("guild server services crashing");
            // TODO send chat message for reconnecting if services crash
            enabled = false;
        }

        public boolean getGuildServerToken() {
            if (wynnPlayerInfo != null) {
                try {
                    JsonObject requestBody = new JsonObject();
                    requestBody.add("validationKey", GuildApi.secrets.get("validation_key"));
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(GuildApi.secrets.get("guild_raid_urls").getAsJsonObject().get(wynnPlayerInfo.get("guild").getAsJsonObject().get("prefix").getAsString()).getAsString() + "auth/getToken"))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                            .build();
                    HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() / 100 == 2) {
                        GuildApi.LOGGER.info("Api token refresh call successful: {} {}", response.body(), response.statusCode());
                        JsonObject responseObject = GuildApi.gson.fromJson(response.body(), JsonObject.class);
                        token = responseObject.get("token").getAsString();
                        return true;
                    }
                    GuildApi.LOGGER.error("get token error: status {} {}", response.statusCode(), response.body());
                } catch (JsonSyntaxException e) {
                    GuildApi.LOGGER.error("Json syntax exception: {}", (Object) e.getStackTrace());
                } catch (Exception e) {
                    GuildApi.LOGGER.error("get token error: {}", e.getMessage());
                }
            } else {
                GuildApi.LOGGER.warn("wynn player not initialized, can't refresh token");
            }
            return false;
        }

        private HttpResponse<?> tryToken(HttpRequest.Builder builder, HttpResponse.BodyHandler<?> bodyHandler) throws IOException, InterruptedException {
            HttpResponse<?> response = HTTP_CLIENT.send(builder.build(), bodyHandler);
            if (response.statusCode() == 401) {
                GuildApi.LOGGER.info("Refreshing api token");
                if (!getGuildServerToken()) {
                    crash();
                    return response;
                }
                builder.setHeader("Authorization", "bearer " + token);
                response = HTTP_CLIENT.send(builder.build(), bodyHandler);
            }
            return response;
        }

        public void post(String path, JsonObject body) {
            if (!enabled) {
                GuildApi.LOGGER.warn("skipped api post because api service were crashed");
                return;
            }
            new Thread(() -> {
                HttpRequest.Builder builder = HttpRequest.newBuilder()
                        .uri(URI.create(BaseURL + path))
                        .headers("Content-Type", "application/json", "Authorization", "bearer " + token)
                        .POST(HttpRequest.BodyPublishers.ofString(body.toString()));
                try {
                    @SuppressWarnings("unchecked")
                    HttpResponse<String> response = (HttpResponse<String>) tryToken(builder, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() / 100 == 2) {
                        GuildApi.LOGGER.info("api POST successful with response {}", response.body());
                    } else {
                        GuildApi.LOGGER.error("api POST error: {} {}", response.statusCode(), response.body());
                    }
                } catch (Exception e) {
                    GuildApi.LOGGER.error("api POST exception: {} {}", e, e.getMessage());
                }
            }, "Api post thread").start();
        }

        public synchronized void delete(String path) {
            if (!enabled) {
                GuildApi.LOGGER.warn("Skipped api delete because api services were crashed");
                return;
            }
            new Thread(() -> {
                HttpRequest.Builder builder = HttpRequest.newBuilder()
                        .uri(URI.create(BaseURL + path))
                        .header("Authorization", "bearer " + token)
                        .DELETE();
                try {
                    @SuppressWarnings("unchecked")
                    HttpResponse<String> response = (HttpResponse<String>) tryToken(builder, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() / 100 == 2) {
                        GuildApi.LOGGER.info("api delete successful");
                    } else {
                        GuildApi.LOGGER.error("api delete failed with status {} {}", response.statusCode(), response.body());
                    }
                } catch (Exception e) {
                    GuildApi.LOGGER.error("api delete error: {} {}", e, e.getMessage());
                }
            }, "api delete thread").start();
        }
    }
}
