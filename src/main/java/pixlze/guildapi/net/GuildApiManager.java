package pixlze.guildapi.net;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.net.type.Api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class GuildApiManager extends Api {
    private final Text reconnectMessage = Text.literal("Could not connect to guild server. Click ").setStyle(Style.EMPTY.withColor(Formatting.RED))
            .append(Text.literal("here").setStyle(Style.EMPTY.withUnderline(true).withColor(Formatting.RED)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/retryLastRequest")))).
            append(Text.literal(" to reconnect.").setStyle(Style.EMPTY.withColor(Formatting.RED)));
    private String token;
    private JsonObject wynnPlayerInfo;

    public GuildApiManager() {
        super("guild", List.of(Managers.Api.getApi("wynn", WynnApiManager.class)));
    }

    public void setBaseUrl(String url) {
        baseURL = url;
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
                HttpResponse<String> response = ApiManager.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
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
        HttpResponse<?> response = ApiManager.HTTP_CLIENT.send(builder.build(), bodyHandler);
        if (response.statusCode() == 401) {
            GuildApi.LOGGER.info("Refreshing api token");
            if (!getGuildServerToken()) {
                crash();
                return response;
            }
            builder.setHeader("Authorization", "bearer " + token);
            response = ApiManager.HTTP_CLIENT.send(builder.build(), bodyHandler);
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
                    .uri(URI.create(baseURL + path))
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
        }, "API post thread").start();
    }

    public void delete(String path) {
        if (!enabled) {
            GuildApi.LOGGER.warn("Skipped api delete because api services were crashed");
            return;
        }
        new Thread(() -> {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseURL + path))
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
        }, "API delete thread").start();
    }

    @Override
    public void init() {
        super.init();

    }
}
