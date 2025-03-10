package pixlze.guildapi.net;

import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.Managers;
import pixlze.guildapi.core.handlers.connection.event.WynncraftConnectionEvents;
import pixlze.guildapi.mc.event.WynnChatMessage;
import pixlze.guildapi.net.type.Api;
import pixlze.guildapi.utils.JsonUtils;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.text.TextUtils;
import pixlze.guildapi.utils.text.type.TextParseOptions;
import pixlze.guildapi.utils.type.Prepend;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedList;
import java.util.Objects;
import java.util.regex.Pattern;

public class WynnApiClient extends Api {
    private static WynnApiClient instance;
    public JsonObject wynnPlayerInfo;
    private boolean reloading = false;
    private final Pattern GUILD_JOIN_PATTERN = Pattern.compile("^§.You have joined §.(?<guild>.+)§.!$");

    protected WynnApiClient() {
        super("wynn", new LinkedList<>());
        instance = this;
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
            if (GuildApi.isTesting()) {
                dispatcher.register(ClientCommandManager.literal("setplayer").then(ClientCommandManager.argument("username", StringArgumentType.word()).executes(context -> {
                    McUtils.devName = StringArgumentType.getString(context, "username");
                    reloadWynnInfo();
                    return Command.SINGLE_SUCCESS;
                })));
            }
        });
        WynnChatMessage.EVENT.register(this::onWynnMessage);
        WynncraftConnectionEvents.JOIN.register(this::onWynnJoin);
    }

    private void onWynnMessage(Text message) {
        if (GUILD_JOIN_PATTERN.matcher(TextUtils.parseStyled(message, TextParseOptions.DEFAULT)).find()) {
            GuildApi.LOGGER.info("joining guild");
            reloadWynnInfo();
        }
    }

    public void initWynnPlayerInfo(boolean print) {
        if (McUtils.mc().player != null) {
            try {
                URI uri = URI.create("https://api.wynncraft.com/v3/player/" + McUtils.playerName());
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(uri)
                        .build();
                HttpResponse<String> response = NetManager.HTTP_CLIENT.send(request,
                        HttpResponse.BodyHandlers.ofString());
                wynnPlayerInfo = JsonUtils.toJsonObject(response.body());
                if (wynnPlayerInfo.get("Error") != null) {
                    String message = wynnPlayerInfo.get("Error").getAsString();
                    wynnPlayerInfo = null;
                    throw new Exception(message);
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
        } else {
            GuildApi.LOGGER.warn("null player found when initializing wynn api");
        }
    }

    private void reloadWynnInfo() {
        this.disable();
        initWynnPlayerInfo(false);
    }

    private void onWynnJoin() {
        if (wynnPlayerInfo == null || !Objects.equals(McUtils.playerUUID(), wynnPlayerInfo.get("uuid").getAsString())) {
            reloadWynnInfo();
        } else {
            GuildApi.LOGGER.warn("wynn player already initialized");
        }
    }
}
