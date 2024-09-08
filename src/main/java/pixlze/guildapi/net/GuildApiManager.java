package pixlze.guildapi.net;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.net.event.WynnApiEvents;
import pixlze.guildapi.net.type.Api;
import pixlze.guildapi.utils.McUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class GuildApiManager extends Api {
    private final Text retryMessage = Text.literal("Could not connect to guild server. Click ")
            .setStyle(Style.EMPTY.withColor(Formatting.RED))
            .append(Text.literal("here").setStyle(
                    Style.EMPTY.withUnderline(true).withColor(Formatting.RED)
                            .withClickEvent(
                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                            "/retryLastFailed")))).
            append(Text.literal(" to retry.")
                    .setStyle(Style.EMPTY.withColor(Formatting.RED)));
    private final Text successMessage = Text.literal("Success!").setStyle(Style.EMPTY.withColor(Formatting.GREEN));
    private final List<String> nonErrors = List.of("User could not be found in tome list.", "duplicate raid",
            "User already in tome list.");
    private final List<String> printNonErrors = List.of("User already in tome list.");
    private String token;
    private JsonObject wynnPlayerInfo;
    private HttpRequest.Builder lastFailed = null;
    private HttpResponse.BodyHandler<?> lastBodyHandler = null;
    private boolean retrying = false;

    public GuildApiManager() {
        super("guild", List.of(Managers.Api.getApi("wynn", WynnApiManager.class)));
    }

    public boolean getGuildServerToken() {
        if (wynnPlayerInfo != null) {
            try {
                JsonObject requestBody = new JsonObject();
                requestBody.add("validationKey", GuildApi.secrets.get("validation_key"));
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(
                                GuildApi.secrets.get("guild_raid_urls").getAsJsonObject()
                                        .get(wynnPlayerInfo.get("guild")
                                                .getAsJsonObject()
                                                .get("prefix")
                                                .getAsString())
                                        .getAsString() + "auth/getToken"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                        .build();
                HttpResponse<String> response = ApiManager.HTTP_CLIENT.send(request,
                        HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() / 100 == 2) {
                    GuildApi.LOGGER.info("Api token refresh call successful: {}", response.statusCode());
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
                return response;
            }
            builder.setHeader("Authorization", "bearer " + token);
            response = ApiManager.HTTP_CLIENT.send(builder.build(), bodyHandler);
        }
        return response;
    }

    private String tryExtractError(String body) {
        String out = null;
        try {
            if (GuildApi.gson.fromJson(body, JsonObject.class).get("error") != null)
                out = GuildApi.gson.fromJson(body, JsonObject.class).get("error").getAsString();
            else {
                out = "";
            }
        } catch (Exception e) {
            GuildApi.LOGGER.error("Extract error exception: {} {}", e, e.getMessage());
        }
        return out;
    }

    private void successMessage() {
        McUtils.sendLocalMessage(successMessage);
    }

    private boolean isError(String error) {
        return !nonErrors.contains(error);
    }

    private boolean printNonError(String error) {
        return printNonErrors.contains(error);
    }

    private void checkError(HttpResponse<?> response, HttpRequest.Builder builder,
            HttpResponse.BodyHandler<?> handler, boolean print) {
        String error = tryExtractError((String) response.body());
        if (error != null) {
            if (isError(error)) {
                lastFailed = builder;
                lastBodyHandler = handler;
                GuildApi.LOGGER.error("API error: {}", error);
                McUtils.sendLocalMessage(retryMessage);
            } else {
                lastFailed = null;
                lastBodyHandler = null;
                GuildApi.LOGGER.warn("API non error: {}", error);
                if (printNonError(error))
                    McUtils.sendLocalMessage(Text.literal(error).setStyle(Style.EMPTY.withColor(Formatting.YELLOW)));
                else if (print)
                    successMessage();

            }
        } else {
            lastFailed = builder;
            lastBodyHandler = HttpResponse.BodyHandlers.ofString();
            McUtils.sendLocalMessage(retryMessage);
        }
    }

    public JsonElement get(String path) {
        if (!enabled) {
            GuildApi.LOGGER.warn("skipped api get because api service were crashed");
            // TODO add click to reload dependencies and rerun request here
            McUtils.sendLocalMessage(Text.literal("A request was skipped.")
                    .setStyle(Style.EMPTY.withColor(Formatting.YELLOW)));
            return null;
        }
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(baseURL + path)).GET();
        JsonElement out = null;
        try {
            HttpResponse<String> response = ApiManager.HTTP_CLIENT.send(builder.build(),
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 == 2)
                out = GuildApi.gson.fromJson(response.body(), JsonElement.class);
            else checkError(response, builder, HttpResponse.BodyHandlers.ofString(), true);
        } catch (Exception e) {
            GuildApi.LOGGER.error("api GET exception {} {} ", e, e.getMessage());
        }
        return out;
    }

    public void post(String path, JsonObject body, boolean print) {
        if (!enabled) {
            GuildApi.LOGGER.warn("skipped api post because api service were crashed");
            // TODO add click to reload dependencies and rerun request here
            McUtils.sendLocalMessage(Text.literal("A request was skipped.")
                    .setStyle(Style.EMPTY.withColor(Formatting.YELLOW)));
            return;
        }
        new Thread(() -> {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseURL + path))
                    .headers("Content-Type", "application/json", "Authorization",
                            "bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()));
            try {
                @SuppressWarnings("unchecked")
                HttpResponse<String> response = (HttpResponse<String>) tryToken(builder,
                        HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() / 100 == 2) {
                    GuildApi.LOGGER.info("api POST successful with response {}", response.body());
                    if (print) successMessage();
                } else {
                    checkError(response, builder, HttpResponse.BodyHandlers.ofString(), print);
                }
            } catch (Exception e) {
                GuildApi.LOGGER.error("api POST exception: {} {}", e, e.getMessage());
            }
        }, "API post thread").start();
    }

    public void delete(String path, boolean print) {
        if (!enabled) {
            GuildApi.LOGGER.warn("Skipped api delete because api services weren't enabled");
            McUtils.sendLocalMessage(Text.literal("A request was skipped.")
                    .setStyle(Style.EMPTY.withColor(Formatting.YELLOW)));
            return;
        }
        new Thread(() -> {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseURL + path))
                    .header("Authorization", "bearer " + token)
                    .DELETE();
            try {
                @SuppressWarnings("unchecked")
                HttpResponse<String> response = (HttpResponse<String>) tryToken(builder,
                        HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() / 100 == 2) {
                    GuildApi.LOGGER.info("api delete successful");
                    if (print) successMessage();
                } else {
                    checkError(response, builder, HttpResponse.BodyHandlers.ofString(), print);
                }
            } catch (Exception e) {
                GuildApi.LOGGER.error("api delete error: {} {}", e, e.getMessage());
            }
        }, "API delete thread").start();
    }

    private void wynnPlayerLoaded() {
        crashed = false;
        wynnPlayerInfo = Managers.Api.getApi("wynn", WynnApiManager.class).wynnPlayerInfo;
        try {
            baseURL = GuildApi.secrets.get("guild_raid_urls").getAsJsonObject()
                    .get(wynnPlayerInfo.get("guild").getAsJsonObject().get("prefix").getAsString())
                    .getAsString();
        } catch (Exception e) {
            // TODO implement retry when actually using a server for guild base urls.
            String guildString = null;
            if (wynnPlayerInfo.get("guild").isJsonObject()) {
                guildString = wynnPlayerInfo.get("guild").getAsJsonObject().get("prefix").getAsString();
            }
            Managers.Api.apiCrash(Text.literal(
                            "Couldn't fetch base url for server of guild \"" + guildString + "\". " +
                                    "Talk to a chief about setting one up for your guild.")
                    .setStyle(Style.EMPTY.withColor(Formatting.RED)), this);
        }
        super.init();
    }

    @Override
    public void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registry) -> dispatcher.register(
                ClientCommandManager.literal("retryLastFailed").executes((context) -> {
                    if (lastFailed == null) return 0;
                    if (!retrying) {
                        new Thread(() -> {
                            retrying = true;
                            McUtils.sendLocalMessage(
                                    Text.literal("Retrying...").setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
                            try {
                                HttpResponse<?> response = tryToken(lastFailed, lastBodyHandler);
                                if (response.statusCode() / 100 == 2) {
                                    McUtils.sendLocalMessage(successMessage);
                                    lastFailed = null;
                                    lastBodyHandler = null;
                                } else {
                                    checkError(response, lastFailed, lastBodyHandler, true);
                                }
                            } catch (Exception e) {
                                GuildApi.LOGGER.error("Retry exception: {} {}", e, e.getMessage());
                            }
                            retrying = false;
                        }).start();
                    }
                    return 0;
                })));
        WynnApiEvents.SUCCESS.register(this::wynnPlayerLoaded);
    }
}
