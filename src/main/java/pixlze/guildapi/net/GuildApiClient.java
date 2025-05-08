package pixlze.guildapi.net;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
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
import pixlze.guildapi.utils.ColourUtils;
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

import static pixlze.guildapi.GuildApi.MOD_VERSION;

public class GuildApiClient extends Api {
    private static final File CACHE_DIR = GuildApi.getModStorageDir("apicache");
    private static final int PORT = 24242;
    private static final String CALLBACK_PATH = "/callback/";
    private static final String REDIRECT_URI = "http://localhost:" + PORT + CALLBACK_PATH;
    private static final String CLIENT_ID = "1252463028025426031";
    private static final String UNLINKED_ERROR = "Could not validate account linking.";
    public static final String API_DISABLED_ERROR = "API is disabled";
    private static final Pattern GUILD_JOIN_PATTERN = Pattern.compile("^§.You have joined §.(?<guild>.+)§.!$");
    private static final Text LOGIN_MESSAGE_NEW = Text.literal("§a§lGuild API §r§av" + MOD_VERSION + " by §lpixlze§r§a.\n§fType /guildapi help for a list of commands.\n§aType /link in your guild's discord bridging channel, then, click ")
            .append(Text.literal("here").setStyle(
                    Style.EMPTY.withUnderline(true).withColor(ColourUtils.GREEN.getColor())
                            .withClickEvent(
                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                            "/gapi login")))).
            append(Text.literal("§a to authenticate and enable most features."));
    private static final Text LOGIN_MESSAGE = Text.literal("§cCould not connect to guild server. Click ")
            .append(Text.literal("here").setStyle(
                    Style.EMPTY.withUnderline(true).withColor(Formatting.RED)
                            .withClickEvent(
                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                            "/gapi login")))).
            append(Text.literal("§c to re-authenticate."));
    private static final Text LINK_MESSAGE = Text.literal("§cYou have not linked a discord account. Type /link in your guild's discord bridging channel, then click ")
            .append(Text.literal("here").setStyle(
                    Style.EMPTY.withUnderline(true).withColor(Formatting.RED)
                            .withClickEvent(
                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                            "/gapi login")))).
            append(Text.literal("§c to re-authenticate."));
    private static final Text SUCCESS_MESSAGE = Text.literal("Success!").setStyle(Style.EMPTY.withColor(Formatting.GREEN));
    private static final String API_BASE_PATH = "api/v3/";
    private static GuildApiClient instance;
    public String guildPrefix = "";
    public String guildId = "none";
    private String token;
    private String refreshToken;
    private JsonObject refreshTokenObject;
    private final File refreshTokenFile;
    private JsonObject wynnPlayerInfo;
    private HttpServer server;

    public GuildApiClient() {
        super("guild", List.of(WynnApiClient.class, WynnJoinApi.class));
        instance = this;
        baseURL = "https://ico-server.onrender.com/";
        baseURL = "http://localhost:3000/";
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
        wynnPlayerInfo = Managers.Net.wynn.wynnPlayerInfo;
        guildPrefix = wynnPlayerInfo.get("guild").getAsJsonObject().get("prefix").getAsString();
        guildId = wynnPlayerInfo.get("guild").getAsJsonObject().get("uuid").getAsString();
        try {
            refreshToken = refreshTokenObject.get("do not share").getAsString();
            if (!getGuildServerToken())
                promptLogin();
            else this.enable();
        } catch (NullPointerException exception) {
            GuildApi.LOGGER.warn("expected nullpointer: {} {}", exception, exception.getMessage());
            McUtils.sendLocalMessage(LOGIN_MESSAGE_NEW, Prepend.DEFAULT.get(), false);
        } catch (Exception e) {
            ExceptionUtils.defaultException("login", e);
            promptLogin();
        }
    }

    protected void unready() {
        wynnPlayerInfo = null;
        guildId = null;
        token = null;
        refreshToken = null;
        super.unready();
    }

    public static GuildApiClient getInstance() {
        return instance;
    }

    public String getToken(boolean refresh) {
        if (token == null || refresh) getGuildServerToken();
        return token;
    }

    public void promptLogin() {
        McUtils.sendLocalMessage(LOGIN_MESSAGE, Prepend.DEFAULT.get(), false);
    }

    public void promptLink() {
        McUtils.sendLocalMessage(LINK_MESSAGE, Prepend.DEFAULT.get(), false);
    }

    public void login() {
        if (server != null) server.stop(0);
        CompletableFuture<Pair<String, String>> tokenRequest = new CompletableFuture<>();
        try {
            startLocalServer(tokenRequest);
            openInBrowser();
        } catch (Exception e) {
            GuildApi.LOGGER.error("its cooked: {} {}", e, e.getMessage());
            tokenRequest.completeExceptionally(e);
        }
        tokenRequest.whenCompleteAsync((res, exception) -> {
            if (exception != null) {
                GuildApi.LOGGER.error("login error: {} {}", exception, exception.getMessage());
                return;
            }
            this.token = res.getLeft();
            this.refreshToken = res.getRight();
            this.saveRefreshToken();
            successMessage();
            super.enable();
        });
    }

    private void handleHttpCallback(HttpExchange exchange, CompletableFuture<Pair<String, String>> tokenRequest) {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = parseQuery(query);
        String code = params.get("code");

        JsonObject requestBody = new JsonObject();
        requestBody.add("grant_type", Managers.Json.toJsonElement("authorization_code"));
        requestBody.add("code", Managers.Json.toJsonElement(code));
        requestBody.add("mcUsername", Managers.Json.toJsonElement(McUtils.playerName()));
        post("auth/get-token", requestBody, true).whenCompleteAsync((res, exception) -> {
            try {
                NetUtils.applyDefaultCallback(res, exception, (resOK) -> {
                            JsonObject resBody = resOK.getAsJsonObject();
                            tokenRequest.complete(new Pair<>(resBody.get("token").getAsString(), resBody.get("refreshToken").getAsString()));
                        },
                        (error) -> {
                            if (error.equals(UNLINKED_ERROR)) {
                                promptLink();
                            } else {
                                McUtils.sendLocalMessage(
                                        Text.literal("§cSomething went wrong authenticating. Click ").append(
                                                Text.literal("here").setStyle(
                                                        Style.EMPTY.withUnderline(true).withColor(Formatting.RED).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/gapi login")))
                                        ).append(Text.literal("§c to try again.")),
                                        Prepend.DEFAULT.get(), false
                                );
                            }
                        }
                );
            } catch (Exception e) {
                GuildApi.LOGGER.error("handle login callback failed: {} {}", e, e.getMessage());
                promptLogin();
            }
            try {
                String html = "You can now close this window and return to the minecraft";
                byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=UTF-8");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } catch (Exception e) {
                ExceptionUtils.defaultException("send success", e);
            }
            exchange.getHttpContext().getServer().stop(0);
        });
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
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
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
        try {
            JsonObject requestBody = new JsonObject();
            requestBody.add("grant_type", Managers.Json.toJsonElement("refresh_token"));
            requestBody.add("refreshToken", Managers.Json.toJsonElement(refreshToken));
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseURL + API_BASE_PATH + "auth/get-token"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()));
            if (GuildApi.isDevelopment()) builder.version(HttpClient.Version.HTTP_1_1);
            HttpResponse<String> response = NetManager.HTTP_CLIENT.send(builder.build(),
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 == 2) {
                GuildApi.LOGGER.info("Api token refresh call successful: {}", response.statusCode());
                JsonObject responseObject = Managers.Json.toJsonObject(response.body());
                token = responseObject.get("token").getAsString();
                refreshToken = responseObject.get("refreshToken").getAsString();
                saveRefreshToken();
                return true;
            }
            GuildApi.LOGGER.error("get token error: status {} {}", response.statusCode(), response.body());
        } catch (JsonSyntaxException e) {
            GuildApi.LOGGER.error("Json syntax exception: {}", (Object) e.getStackTrace());
        } catch (Exception e) {
            GuildApi.LOGGER.error("get token error: {}", e.getMessage());
        }
        if (!this.isDisabled())
            Managers.Net.apiCrash(LOGIN_MESSAGE, this);
        return false;
    }

    private void applyCallback(CompletableFuture<HttpResponse<String>> callback, HttpResponse<String> response, Throwable exception) {
        if (exception != null) {
            callback.completeExceptionally(exception);
            return;
        }
        callback.complete(response);
    }

    public CompletableFuture<HttpResponse<String>> get(String path, boolean skipDisableCheck) {
        path = API_BASE_PATH + path;
        CompletableFuture<HttpResponse<String>> out = new CompletableFuture<>();
        if (isDisabled() && !skipDisableCheck) {
            GuildApi.LOGGER.warn("skipped api call because not logged in");
            promptLogin();
            out.completeExceptionally(new Exception(API_DISABLED_ERROR));
            return out;
        }
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(baseURL + path))
                .header("Authorization", "bearer " + token).GET();
        if (GuildApi.isDevelopment()) builder.version(HttpClient.Version.HTTP_1_1);
        CompletableFuture<HttpResponse<String>> response = tryToken(builder);
        response.whenCompleteAsync((res, exception) -> {
                    GuildApi.LOGGER.info("api GET completed: res {} exception {}", res.body(), exception);
                    applyCallback(out, res, exception);
                }
        );
        return out;
    }

    public CompletableFuture<HttpResponse<String>> post(String path, JsonObject body, boolean skipDisableCheck) {
        CompletableFuture<HttpResponse<String>> out = new CompletableFuture<>();
        path = API_BASE_PATH + path;
        if (isDisabled() && !skipDisableCheck) {
            GuildApi.LOGGER.warn("skipped api post because api service were crashed");
            promptLogin();
            out.completeExceptionally(new Exception(API_DISABLED_ERROR));
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
            GuildApi.LOGGER.info("api POST completed: res {} exception {}", res.body(), exception);
            applyCallback(out, res, exception);
        });
        return out;
    }

    public CompletableFuture<HttpResponse<String>> delete(String path) {
        CompletableFuture<HttpResponse<String>> out = new CompletableFuture<>();
        path = API_BASE_PATH + path;
        if (isDisabled()) {
            GuildApi.LOGGER.warn("Skipped api delete because api services weren't enabled");
            promptLogin();
            return out;
        }
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + path))
                .header("Authorization", "bearer " + token)
                .DELETE();
        if (GuildApi.isDevelopment()) builder.version(HttpClient.Version.HTTP_1_1);
        CompletableFuture<HttpResponse<String>> response = tryToken(builder);
        response.whenCompleteAsync((res, exception) -> {
            GuildApi.LOGGER.info("api DELETE completed: res {} exception {}", res.body(), exception);
            applyCallback(out, res, exception);
        });
        return out;
    }


    private void successMessage() {
        McUtils.sendLocalMessage(SUCCESS_MESSAGE, Prepend.DEFAULT.get(), false);
    }

    public String getBaseURL() {
        return baseURL;
    }

    private void saveRefreshToken() {
        this.refreshTokenObject.addProperty("do not share", refreshToken);
        Managers.Json.saveJsonAsFile(refreshTokenFile, refreshTokenObject);
    }

    /** only used for test command */
    public void resetToken() {
        this.token = null;
    }

    /** only used for test command */
    public void resetRefreshToken() {
        this.token = null;
        this.refreshToken = null;
    }
}
