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
import pixlze.guildapi.net.type.Api;
import pixlze.guildapi.utils.McUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GuildApiClient extends Api {
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
            "User already in tome list.", "Specified user could not be found in tome list.", "Specified user could not be found in aspect list.");
    private final List<String> printNonErrors = List.of("Specified user could not be found in tome list.", "User already in tome list.", "Specified user could not be found in aspect list.");
    private String token;
    private JsonObject wynnPlayerInfo;
    private HttpRequest.Builder lastFailed = null;
    private CompletableFuture<JsonElement> failedPromise = null;
    private boolean retrying = false;

    public GuildApiClient() {
        super("guild", List.of(WynnApiClient.class));
    }

    public String getToken() {
        return token;
    }

    public CompletableFuture<JsonElement> get(String path) {
        CompletableFuture<JsonElement> out = new CompletableFuture<>();
        if (!enabled) {
            GuildApi.LOGGER.warn("skipped api get because api service were crashed");
            McUtils.sendLocalMessage(Text.literal("A request was skipped.")
                    .setStyle(Style.EMPTY.withColor(Formatting.YELLOW)));
            failedPromise = out;
            return out;
        }
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(baseURL + path)).GET();
        if (GuildApi.isDevelopment()) builder.version(HttpClient.Version.HTTP_1_1);
        CompletableFuture<HttpResponse<String>> response = NetManager.HTTP_CLIENT.sendAsync(builder.build(),
                HttpResponse.BodyHandlers.ofString());
        response.whenCompleteAsync((res, exception) -> {
            if (exception != null) {
                assert Formatting.RED.getColorValue() != null;
                McUtils.sendLocalMessage(Text.literal("Fatal API error: " + exception + " " + exception.getMessage())
                        .withColor(Formatting.RED.getColorValue()));
                lastFailed = builder;
                McUtils.sendLocalMessage(retryMessage);
                GuildApi.LOGGER.error("api GET exception {} {} ", exception, exception.getMessage());
                failedPromise = out;
            } else {
                if (res.statusCode() / 100 == 2)
                    out.complete(Managers.Json.toJsonElement(res.body()));
                else checkError(res, builder, false);
            }
        });
        return out;
    }

    private void checkError(HttpResponse<String> response, HttpRequest.Builder builder,
                            boolean print) {
        String error = tryExtractError(response.body());
        if (error != null) {
            if (isError(error)) {
                lastFailed = builder;
                GuildApi.LOGGER.error("API error: {}", error);
                McUtils.sendLocalMessage(retryMessage);
            } else {
                if (failedPromise != null) {
                    failedPromise.complete(Managers.Json.toJsonElement(response.body()));
                    failedPromise = null;
                }
                lastFailed = null;
                GuildApi.LOGGER.warn("API non error: {}", error);
                if (printNonError(error))
                    McUtils.sendLocalMessage(Text.literal(error).setStyle(Style.EMPTY.withColor(Formatting.YELLOW)));
                else if (print)
                    successMessage();

            }
        } else {
            lastFailed = builder;
            McUtils.sendLocalMessage(retryMessage);
        }
    }

    private String tryExtractError(String body) {
        GuildApi.LOGGER.info(body);
        String out = null;
        try {
            if (Managers.Json.toJsonObject(body).get("error") != null)
                out = Managers.Json.toJsonObject(body).get("error").getAsString();
            else {
                out = "";
            }
        } catch (Exception e) {
            GuildApi.LOGGER.error("Extract error exception: {} {}", e, e.getMessage());
        }
        return out;
    }

    private boolean isError(String error) {
        return !nonErrors.contains(error);
    }

    private boolean printNonError(String error) {
        return printNonErrors.contains(error);
    }

    private void successMessage() {
        McUtils.sendLocalMessage(successMessage);
    }

    public void post(String path, JsonObject body, boolean print) {
        if (!enabled) {
            GuildApi.LOGGER.warn("skipped api post because api service were crashed");
            McUtils.sendLocalMessage(Text.literal("A request was skipped.")
                    .setStyle(Style.EMPTY.withColor(Formatting.YELLOW)));
            return;
        }
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + path))
                .headers("Content-Type", "application/json", "Authorization",
                        "bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()));
        if (GuildApi.isDevelopment()) builder.version(HttpClient.Version.HTTP_1_1);
        CompletableFuture<HttpResponse<String>> response = tryToken(builder);
        response.whenCompleteAsync((res, exception) -> {
            if (exception != null) {
                assert Formatting.RED.getColorValue() != null;
                McUtils.sendLocalMessage(Text.literal("Fatal API error: " + exception + " " + exception.getMessage())
                        .withColor(Formatting.RED.getColorValue()));
                lastFailed = builder;
                McUtils.sendLocalMessage(retryMessage);
                GuildApi.LOGGER.error("api POST exception: {} {}", exception, exception.getMessage());
            } else {
                if (res.statusCode() / 100 == 2) {
                    GuildApi.LOGGER.info("api POST successful with response {}", res.body());
                    if (print) successMessage();
                } else {
                    checkError(res, builder, print);
                }
            }
        });
    }

    private CompletableFuture<HttpResponse<String>> tryToken(HttpRequest.Builder builder) {
        CompletableFuture<HttpResponse<String>> response = NetManager.HTTP_CLIENT.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString());
        CompletableFuture<HttpResponse<String>> out = new CompletableFuture<>();
        response.whenCompleteAsync((res, exception) -> {
            if (exception != null) {
                out.completeExceptionally(exception);
            } else {
                if (res.statusCode() == 401) {
                    GuildApi.LOGGER.info("Refreshing api token");
                    if (!getGuildServerToken()) {
                        out.complete(res);
                        return;
                    }
                }
                builder.setHeader("Authorization", "bearer " + token);
                try {
                    HttpResponse<String> res2 = NetManager.HTTP_CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString());
                    out.complete(res2);
                } catch (Exception e) {
                    out.completeExceptionally(e);
                }
            }
        });
        return out;
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
                HttpResponse<String> response = NetManager.HTTP_CLIENT.send(request,
                        HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() / 100 == 2) {
                    GuildApi.LOGGER.info("Api token refresh call successful: {}", response.statusCode());
                    JsonObject responseObject = Managers.Json.toJsonObject(response.body());
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

    public void delete(String path, boolean print) {
        if (!enabled) {
            GuildApi.LOGGER.warn("Skipped api delete because api services weren't enabled");
            McUtils.sendLocalMessage(Text.literal("A request was skipped.")
                    .setStyle(Style.EMPTY.withColor(Formatting.YELLOW)));
            return;
        }
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + path))
                .header("Authorization", "bearer " + token)
                .DELETE();
        if (GuildApi.isDevelopment()) builder.version(HttpClient.Version.HTTP_1_1);
        CompletableFuture<HttpResponse<String>> response = tryToken(builder);
        response.whenCompleteAsync((res, exception) -> {
            if (exception != null) {
                assert Formatting.RED.getColorValue() != null;
                McUtils.sendLocalMessage(Text.literal("Fatal API error: " + exception + " " + exception.getMessage())
                        .withColor(Formatting.RED.getColorValue()));
                lastFailed = builder;
                McUtils.sendLocalMessage(retryMessage);
                GuildApi.LOGGER.error("api delete error: {} {}", exception, exception.getMessage());
            } else {
                if (res.statusCode() / 100 == 2) {
                    GuildApi.LOGGER.info("api delete successful");
                    if (print) successMessage();
                } else {
                    checkError(res, builder, print);
                }
            }
        });
    }

    public String getBaseURL() {
        return baseURL;
    }

    @Override
    public void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registry) -> dispatcher.register(
                ClientCommandManager.literal("retryLastFailed").executes((context) -> {
                    if (lastFailed == null) return 0;
                    if (!retrying) {
                        retrying = true;
                        McUtils.sendLocalMessage(
                                Text.literal("Retrying...").setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
                        CompletableFuture<HttpResponse<String>> response = tryToken(lastFailed);
                        response.whenCompleteAsync((res, exception) -> {
                            GuildApi.LOGGER.info("retrying {} {}", res, exception);
                            if (exception != null) {
                                GuildApi.LOGGER.error("Retry exception: {} {}", exception, exception.getMessage());
                                McUtils.sendLocalMessage(retryMessage);
                            } else {
                                if (res.statusCode() / 100 == 2) {
                                    McUtils.sendLocalMessage(successMessage);
                                    if (failedPromise != null) {
                                        failedPromise.complete(Managers.Json.toJsonElement(res.body()));
                                        failedPromise = null;
                                    }
                                    lastFailed = null;
                                } else {
                                    checkError(res, lastFailed, true);
                                }
                            }
                            retrying = false;
                        });
                    }
                    return 0;
                })));
    }

    @Override
    protected void ready() {
        crashed = false;
        wynnPlayerInfo = Managers.Net.getApi("wynn", WynnApiClient.class).wynnPlayerInfo;
        try {
            if (GuildApi.isDevelopment()) {
                baseURL = "http://localhost:3000/";
            } else {
                baseURL = GuildApi.secrets.get("guild_raid_urls").getAsJsonObject()
                        .get(wynnPlayerInfo.get("guild").getAsJsonObject().get("prefix").getAsString())
                        .getAsString();
            }
            super.init();
        } catch (Exception e) {
            // TODO implement retry when actually using a server for guild base urls.
            String guildString = null;
            if (wynnPlayerInfo.get("guild").isJsonObject()) {
                guildString = wynnPlayerInfo.get("guild").getAsJsonObject().get("prefix").getAsString();
            }
            Managers.Net.apiCrash(Text.literal(
                            "Couldn't fetch base url for server of guild \"" + guildString + "\". " +
                                    "Talk to a chief about setting one up for your guild.")
                    .setStyle(Style.EMPTY.withColor(Formatting.RED)), this);
        }
    }
}
