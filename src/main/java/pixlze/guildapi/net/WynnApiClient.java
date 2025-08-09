package pixlze.guildapi.net;

import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.core.handlers.chat.event.ChatMessageReceived;
import pixlze.guildapi.net.type.Api;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.text.TextUtils;
import pixlze.guildapi.utils.text.type.TextParseOptions;
import pixlze.guildapi.utils.type.Prepend;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WynnApiClient extends Api {
    private static final Pattern GUILD_JOIN_PATTERN = Pattern.compile("^§.You have joined §.(?<guild>.+)§.!$");
    private static WynnApiClient instance;
    public JsonObject wynnPlayerInfo;
    private boolean reloading = false;
    private String expectedGuild;

    private Thread lockThread;


    protected WynnApiClient() {
        super("wynn", List.of());
        instance = this;
        ChatMessageReceived.EVENT.register(this::onWynnMessage);
    }

    public static WynnApiClient getInstance() {
        return instance;
    }

    public void init() {
        // TODO move these to client command classes
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    ClientCommandManager.literal("reloadWynnInfo").executes(context -> {
                        if (!isDisabled() || reloading) return 0;
                        new Thread(() -> {
                            reloading = true;
                            McUtils.sendLocalMessage(
                                    Text.literal("Reloading...")
                                            .setStyle(Style.EMPTY.withColor(Formatting.GREEN)), Prepend.DEFAULT.get(), false);
                            initWynnPlayerInfo(true);
                            reloading = false;
                        }).start();
                        return Command.SINGLE_SUCCESS;
                    }));
        });
        this.ready();
    }

    public void initWynnPlayerInfo(boolean print) {
        try {
            URI uri = URI.create("https://api.wynncraft.com/v3/player/" + McUtils.playerName());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .build();
            HttpResponse<String> response = NetManager.HTTP_CLIENT.send(request,
                    HttpResponse.BodyHandlers.ofString());
            wynnPlayerInfo = Managers.Json.toJsonObject(response.body());
            if (wynnPlayerInfo.get("Error") != null) {
                String message = wynnPlayerInfo.get("Error").getAsString();
                wynnPlayerInfo = null;
                throw new Exception(message);
            }

            // asserts that the necessary fields for guildapiclient exist
            wynnPlayerInfo.get("guild").getAsJsonObject().get("uuid").getAsString();
            String name = wynnPlayerInfo.get("guild").getAsJsonObject().get("name").getAsString();
            wynnPlayerInfo.get("guild").getAsJsonObject().get("prefix").getAsString();

            if (expectedGuild != null && !expectedGuild.equals(name)) {
                tryNewGuild(10000);
                return;
            }

            GuildApi.LOGGER.info("successfully loaded wynn player info");
            if (GuildApi.isDevelopment() || GuildApi.isTesting())
                McUtils.devUUID = wynnPlayerInfo.get("uuid").getAsString();
            if (print)
                McUtils.sendLocalMessage(
                        Text.literal("Success!")
                                .setStyle(Style.EMPTY.withColor(Formatting.GREEN)), Prepend.DEFAULT.get(), false);
            super.enable();
        } catch (Exception e) {
            GuildApi.LOGGER.error("wynn player load error: {} {}", e, e.getMessage());
            Managers.Net.apiCrash(
                    Text.literal("Wynncraft api fetch for " + McUtils.playerName() + " failed. Click ")
                            .setStyle(Style.EMPTY.withColor(Formatting.RED))
                            .append(Text.literal("here")
                                    .setStyle(Style.EMPTY.withUnderline(true).withColor(Formatting.RED)
                                            .withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                                            "/reloadWynnInfo"))))
                            .append(Text.literal(" to retry.").setStyle(Style.EMPTY.withColor(Formatting.RED))),
                    this);
        }
    }

    public void reloadWynnInfo() {
        this.disable();
        initWynnPlayerInfo(false);
    }

    @Override
    protected void ready() {
        if (wynnPlayerInfo == null || !Objects.equals(McUtils.playerUUID(), wynnPlayerInfo.get("uuid").getAsString())) {
            reloadWynnInfo();
        } else {
            GuildApi.LOGGER.warn("wynn player already initialized");
        }
    }

    private void tryNewGuild(int sleepTime) {
        // stops any current running lock threads
        if (lockThread != null)
            lockThread.interrupt();

        this.disable();
        Managers.Tick.scheduleNextTick(() -> McUtils.sendLocalMessage(Text.literal("§ePlease wait ~2 minutes to connect to the chat server while the Wynncraft API updates."), Prepend.DEFAULT.get(), false));

        lockThread = new Thread(() -> {
            reloading = true;

            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                GuildApi.LOGGER.warn("wynnapi lock error: {} {}", e, e.getMessage());
                return;
            }

            reloadWynnInfo();

            reloading = false;
        }, "Wynn Player Info Lock Thread");
        lockThread.start();
    }

    private void onWynnMessage(Text message) {
        Matcher m = GUILD_JOIN_PATTERN.matcher(TextUtils.parseStyled(message, TextParseOptions.DEFAULT));
        if (m.find()) {
            String newGuild = m.group("guild");
            GuildApi.LOGGER.info("joining guild: {}", newGuild);

            Managers.DiscordSocket.disable();

            this.expectedGuild = newGuild;
            tryNewGuild(125000);
        }
    }
}
