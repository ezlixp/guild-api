package pixlze.guildapi.net;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.net.type.Api;
import pixlze.guildapi.utils.ExceptionUtils;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.NetUtils;
import pixlze.guildapi.utils.text.TextUtils;
import pixlze.guildapi.utils.text.type.TextParseOptions;
import pixlze.guildapi.utils.type.Prepend;

import java.awt.*;
import java.io.File;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class GuildApiClient extends Api {
    private static final File CACHE_DIR = GuildApi.getModStorageDir("apicache");
    private static final int PORT = 2424;
    private static final String CALLBACK_PATH = "/callback/";
    private static final String REDIRECT_URI = "http://localhost:" + PORT + CALLBACK_PATH;
    private static final String CLIENT_ID = "1091532517292642367";
    private static final Pattern GUILD_JOIN_PATTERN = Pattern.compile("^§.You have joined §.(?<guild>.+)§.!$");
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
    private JsonObject refreshTokenObject;
    private final File refreshTokenFile;
    private JsonObject wynnPlayerInfo;

    public GuildApiClient() {
        super("guild", List.of(WynnApiClient.class));
        instance = this;
        baseURL = "https://ico-server.onrender.com/";
        refreshTokenFile = new File(CACHE_DIR, "webapi.json");
    }


    private void onWynnMessage(Text message) {
        if (GUILD_JOIN_PATTERN.matcher(TextUtils.parseStyled(message, TextParseOptions.DEFAULT)).find()) {
            GuildApi.LOGGER.info("joining guild");
//            reloadWynnInfo();
            // §3You have joined §bIdiot Co§3!
        }
    }

    @Override
    public void init() {
        refreshTokenObject = Managers.Json.loadJsonFromFile(refreshTokenFile);

    }

    @Override
    protected void ready() {
        try {
            String refreshKey = refreshTokenObject.get("do not share").getAsString();
        } catch (NullPointerException exception) {
            GuildApi.LOGGER.warn("expected nullpointer: {} {}", exception, exception.getMessage());
            login();
        } catch (Exception e) {
            GuildApi.LOGGER.error("get refresh key error: {} {}", e, e.getMessage());
        }
    }

    protected void unready() {
        wynnPlayerInfo = null;
        guildId = null;
        token = null;
        super.unready();
    }

    public static GuildApiClient getInstance() {
        return instance;
    }

    public String getToken(boolean refresh) {
        if (token == null || refresh) getGuildServerToken();
        return token;
    }

    private CompletableFuture<Pair<String, String>> login() {
        // left: token, right: refresh
        CompletableFuture<Pair<String, String>> tokenRequest = new CompletableFuture<>();
        try {
            startLocalServer(tokenRequest);
            openInBrowser();
        } catch (Exception e) {
            GuildApi.LOGGER.error("its cooked: {} {}", e, e.getMessage());
            tokenRequest.completeExceptionally(e);
        }
        return tokenRequest;
    }

    private void handleHttpCallback(HttpExchange exchange, CompletableFuture<Pair<String, String>> tokenRequest) throws java.io.IOException {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = parseQuery(query);
        String code = params.get("code");
        GuildApi.LOGGER.info(code);

        String html = "<html><body><h2>Authorization complete</h2>"
                + "<p>You can now close this window and return to the app.</p>"
                + "</body></html>";
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }

        exchange.getHttpContext().getServer().stop(0);
    }

    private Map<String, String> parseQuery(String q) {
        Map<String, String> m = new HashMap<>();
        if (q == null) return m;
        try {
            for (String pair : q.split("&")) {
                String[] kv = pair.split("=", 2);
                m.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                        kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8):"");
            }
        } catch (Exception e) {
            GuildApi.LOGGER.error("parse query error: {} {}", e, e.getMessage());
        }
        return m;
    }

    private void openInBrowser() throws Exception {
        String state = UUID.randomUUID().toString();
        String authUrl = "https://discord.com/api/v10/oauth2/authorize"
                + "?response_type=code"
                + "&client_id=" + URLEncoder.encode(CLIENT_ID, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8)
                + "&scope=" + URLEncoder.encode("identify", StandardCharsets.UTF_8)
                + "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);

        Desktop.getDesktop().browse(new URI(authUrl));
    }

    private void startLocalServer(CompletableFuture<Pair<String, String>> tokenRequest) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext(CALLBACK_PATH, exchange -> handleHttpCallback(exchange, tokenRequest));
        server.setExecutor(null);
        server.start();
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

    private boolean getGuildServerToken() {
        return false;
//        if (wynnPlayerInfo != null) {
//            try {
//                JsonObject requestBody = new JsonObject();
//                requestBody.add("validationKey", validationKey);
//                requestBody.add("username", JsonUtils.toJsonElement(McUtils.playerName()));
//                HttpRequest.Builder builder = HttpRequest.newBuilder()
//                        .uri(URI.create(baseURL + apiBasePath + "guilds/auth/get-token/" + guildId))
//                        .header("Content-Type", "application/json")
//                        .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()));
//                if (GuildApi.isDevelopment()) builder.version(HttpClient.Version.HTTP_1_1);
//                HttpResponse<String> response = NetManager.HTTP_CLIENT.send(builder.build(),
//                        HttpResponse.BodyHandlers.ofString());
//                if (response.statusCode() / 100 == 2) {
//                    GuildApi.LOGGER.info("Api token refresh call successful: {}", response.statusCode());
//                    JsonObject responseObject = JsonUtils.toJsonObject(response.body());
//                    token = responseObject.get("token").getAsString();
//                    return true;
//                }
//                GuildApi.LOGGER.error("get token error: status {} {}", response.statusCode(), response.body());
//            } catch (JsonSyntaxException e) {
//                GuildApi.LOGGER.error("Json syntax exception: {}", (Object) e.getStackTrace());
//            } catch (Exception e) {
//                GuildApi.LOGGER.error("get token error: {}", e.getMessage());
//            }
//        } else {
//            GuildApi.LOGGER.warn("wynn player not initialized, can't refresh token");
//        }
//        return false;
    }

    private void applyCallback(CompletableFuture<HttpResponse<String>> callback, HttpResponse<String> response, Throwable exception) {
        if (exception != null) {
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


    private void successMessage() {
        McUtils.sendLocalMessage(successMessage, Prepend.DEFAULT.get(), false);
    }

    public String getBaseURL() {
        return baseURL;
    }


}
