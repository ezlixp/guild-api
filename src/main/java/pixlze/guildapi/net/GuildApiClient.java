package pixlze.guildapi.net;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.net.type.Api;
import pixlze.guildapi.utils.JsonUtils;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.NetUtils;
import pixlze.guildapi.utils.type.Prepend;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GuildApiClient extends Api {
    private static GuildApiClient instance;
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
    private final String apiBasePath = "api/v2/";
    public String guildPrefix = "";
    public String guildId = "none";
    private String token;
    private JsonElement validationKey;
    private JsonObject wynnPlayerInfo;

    public GuildApiClient() {
        super("guild", List.of(WynnApiClient.class));
        instance = this;
    }

    public static GuildApiClient getInstance() {
        return instance;
    }

    public String getToken(boolean refresh) {
        if (token == null || refresh) getGuildServerToken();
        return token;
    }

    private boolean getGuildServerToken() {
        if (wynnPlayerInfo != null) {
            try {
                JsonObject requestBody = new JsonObject();
                requestBody.add("validationKey", validationKey);
                HttpRequest.Builder builder = HttpRequest.newBuilder()
                        .uri(URI.create(baseURL + apiBasePath + "guilds/auth/get-token/" + guildId))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()));
                if (GuildApi.isDevelopment()) builder.version(HttpClient.Version.HTTP_1_1);
                HttpResponse<String> response = NetManager.HTTP_CLIENT.send(builder.build(),
                        HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() / 100 == 2) {
                    GuildApi.LOGGER.info("Api token refresh call successful: {}", response.statusCode());
                    JsonObject responseObject = JsonUtils.toJsonObject(response.body());
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

    private void applyCallback(CompletableFuture<HttpResponse<String>> callback, HttpResponse<String> response, Throwable exception) {
        if (exception != null) {
            assert Formatting.RED.getColorValue() != null;
//            McUtils.sendLocalMessage(Text.literal("Fatal API error: " + exception + " " + exception.getMessage())
//                    .withColor(Formatting.RED.getColorValue()), Prepend.DEFAULT.get(), false);
            callback.completeExceptionally(exception);
            return;
        }
        callback.complete(response);
    }

    public CompletableFuture<HttpResponse<String>> get(String path) {
        path = apiBasePath + path;
        CompletableFuture<HttpResponse<String>> out = new CompletableFuture<>();
        if (isDisabled()) {
            GuildApi.LOGGER.warn("skipped api get because api service were crashed");
            McUtils.sendLocalMessage(Text.literal("A request was skipped.")
                    .setStyle(Style.EMPTY.withColor(Formatting.YELLOW)), Prepend.DEFAULT.get(), false);
            return out;
        }
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(baseURL + path))
                .header("Authorization", "bearer " + token).GET();
        if (GuildApi.isDevelopment()) builder.version(HttpClient.Version.HTTP_1_1);
        CompletableFuture<HttpResponse<String>> response = tryToken(builder);
        response.whenCompleteAsync((res, exception) -> {
                    GuildApi.LOGGER.info("api GET completed: res {} exception {}", res, exception);
                    applyCallback(out, res, exception);
                    // else {
//                        if (res.statusCode() / 100 == 2)
//                            out.complete(JsonUtils.toJsonElement(res.body()));
//                        else {
//                            if (handleError)
//                                checkError(res, builder, false);
//                            else out.complete(JsonUtils.toJsonElement(res.body()));
//                        }
//                    }
                }
        );
        return out;
    }

    public CompletableFuture<HttpResponse<String>> post(String path, JsonObject body) {
        CompletableFuture<HttpResponse<String>> out = new CompletableFuture<>();
        path = apiBasePath + path;
        if (isDisabled()) {
            GuildApi.LOGGER.warn("skipped api post because api service were crashed");
            McUtils.sendLocalMessage(Text.literal("A request was skipped.")
                    .setStyle(Style.EMPTY.withColor(Formatting.YELLOW)), Prepend.DEFAULT.get(), false);
            return out;
        }
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + path))
                .headers("Content-Type", "application/json", "Authorization",
                        "bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()));
        if (GuildApi.isDevelopment()) builder.version(HttpClient.Version.HTTP_1_1);
        CompletableFuture<HttpResponse<String>> response = tryToken(builder);
        response.whenCompleteAsync((res, exception) -> {
            GuildApi.LOGGER.info("api POST completed: res {} exception {}", res, exception);
            applyCallback(out, res, exception);
        });
        return out;
    }

    public CompletableFuture<HttpResponse<String>> delete(String path) {
        CompletableFuture<HttpResponse<String>> out = new CompletableFuture<>();
        path = apiBasePath + path;
        if (isDisabled()) {
            GuildApi.LOGGER.warn("Skipped api delete because api services weren't enabled");
            McUtils.sendLocalMessage(Text.literal("A request was skipped.")
                    .setStyle(Style.EMPTY.withColor(Formatting.YELLOW)), Prepend.DEFAULT.get(), false);
            return out;
        }
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + path))
                .header("Authorization", "bearer " + token)
                .DELETE();
        if (GuildApi.isDevelopment()) builder.version(HttpClient.Version.HTTP_1_1);
        CompletableFuture<HttpResponse<String>> response = tryToken(builder);
        response.whenCompleteAsync((res, exception) -> {
            GuildApi.LOGGER.info("api DELETE completed: res {} exception {}", res, exception);
            applyCallback(out, res, exception);
        });
        return out;
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
                    builder.setHeader("Authorization", "bearer " + token);
                    try {
                        HttpResponse<String> res2 = NetManager.HTTP_CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString());
                        out.complete(res2);
                    } catch (Exception e) {
                        out.completeExceptionally(e);
                    }
                } else {
                    out.complete(res);
                }
            }
        });
        return out;
    }

    private void successMessage() {
        McUtils.sendLocalMessage(successMessage, Prepend.DEFAULT.get(), false);
    }


    public String getBaseURL() {
        return baseURL;
    }

    @Override
    public void init() {
    }

    @Override
    protected void ready() {
        wynnPlayerInfo = Managers.Net.wynn.wynnPlayerInfo;
        try {
            guildPrefix = wynnPlayerInfo.get("guild").getAsJsonObject().get("prefix").getAsString();
            guildId = wynnPlayerInfo.get("guild").getAsJsonObject().get("uuid").getAsString();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GuildApi.secrets.get("url").getAsString() + "guild/id/" + guildId))
                    .header("Authorization", "bearer " + GuildApi.secrets.get("password").getAsString())
                    .GET()
                    .build();
            NetManager.HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .whenCompleteAsync((response, error) -> {
                        try {
                            NetUtils.applyDefaultCallback(response, error, (resOK) -> {
                                JsonObject res = JsonUtils.toJsonObject(response.body());
                                baseURL = GuildApi.isDevelopment() ? "http://localhost:3000/":"https://ico-server-test.onrender.com/";
                                validationKey = res.get("validationKey");
                                GuildApi.LOGGER.info("successfully loaded base url");
                                super.enable();
                            }, (e) -> {
                                String guildString = null;
                                if (wynnPlayerInfo.get("guild").isJsonObject()) {
                                    guildString = wynnPlayerInfo.get("guild").getAsJsonObject().get("prefix").getAsString();
                                }
                                Managers.Net.apiCrash(Text.literal(
                                                "Couldn't fetch base url for server of guild \"" + guildString + "\". " +
                                                        "Talk to a chief about setting one up for your guild. If you believe this is a mistake, check logs for more details.")
                                        .setStyle(Style.EMPTY.withColor(Formatting.RED)), this);
                                GuildApi.LOGGER.error("Fetch guild api exception: {}", e);
                            });
                        } catch (Exception e) {
                            String guildString = null;
                            if (wynnPlayerInfo.get("guild").isJsonObject()) {
                                guildString = wynnPlayerInfo.get("guild").getAsJsonObject().get("prefix").getAsString();
                            }
                            Managers.Net.apiCrash(Text.literal(
                                            "Couldn't fetch base url for server of guild \"" + guildString + "\". " +
                                                    "Talk to a chief about setting one up for your guild. If you believe this is a mistake, check logs for more details.")
                                    .setStyle(Style.EMPTY.withColor(Formatting.RED)), this);
                            GuildApi.LOGGER.error("Fetch guild exception: {} {}", e, e.getMessage());

                        }
                    });
        } catch (Exception e) {
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

    protected void unready() {
        validationKey = null;
        wynnPlayerInfo = null;
        guildId = null;
        baseURL = null;
        token = null;
        super.unready();
    }

}
