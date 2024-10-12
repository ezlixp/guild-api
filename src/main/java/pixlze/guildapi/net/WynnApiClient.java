package pixlze.guildapi.net;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.mod.event.WynncraftConnectionEvents;
import pixlze.guildapi.net.type.Api;
import pixlze.guildapi.utils.JsonUtils;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.type.Prepend;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedList;
import java.util.UUID;

public class WynnApiClient extends Api {
    private static WynnApiClient instance;
    public JsonObject wynnPlayerInfo;
    private boolean reloading = false;

    protected WynnApiClient() {
        super("wynn", new LinkedList<>());
        instance = this;
    }

    @Override
    public WynnApiClient getInstance() {
        return instance;
    }

    public void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                ClientCommandManager.literal("reloadWynnInfo").executes(context -> {
                    if (!enabled && !reloading) {
                        new Thread(() -> {
                            reloading = true;
                            McUtils.sendLocalMessage(
                                    Text.literal("Reloading...")
                                            .setStyle(Style.EMPTY.withColor(Formatting.GREEN)), Prepend.DEFAULT.get(), false);
                            GuildApi.LOGGER.info("{}", Managers.Net.wynn.enabled);
                            initWynnPlayerInfo(true);
                            GuildApi.LOGGER.info("{}", Managers.Net.wynn.enabled);
                            reloading = false;
                        }).start();
                    }
                    return 0;
                })));
        WynncraftConnectionEvents.JOIN.register(this::onWynnJoin);
    }

    public void initWynnPlayerInfo(boolean print) {
        if (McUtils.mc().player != null) {
            try {
                URI uri = URI.create(GuildApi.isDevelopment() ? "https://api.wynncraft.com/v3/player/doggc":
                        "https://api.wynncraft.com/v3/player/" + McUtils.mc().player.getUuidAsString());
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(uri)
                        .build();
                HttpResponse<String> response = NetManager.HTTP_CLIENT.send(request,
                        HttpResponse.BodyHandlers.ofString());
                wynnPlayerInfo = JsonUtils.toJsonObject(response.body());
                GuildApi.LOGGER.info("wynn response: {}", wynnPlayerInfo);
                if (wynnPlayerInfo.get("Error") != null) {
                    String message = wynnPlayerInfo.get("Error").getAsString();
                    wynnPlayerInfo = null;
                    throw new Exception(message);
                }
                GuildApi.LOGGER.info("successfully loaded wynn player info");
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

    private void onWynnJoin() {
        if (wynnPlayerInfo == null || !McUtils.player().getUuid()
                .equals(UUID.fromString(wynnPlayerInfo.get("uuid").getAsString()))) {
            this.disable();
            initWynnPlayerInfo(false);
        } else {
            GuildApi.LOGGER.warn("wynn player already initialized");
        }
    }
}
