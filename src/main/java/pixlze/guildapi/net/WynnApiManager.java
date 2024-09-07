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
import pixlze.guildapi.net.event.WynnApiEvents;
import pixlze.guildapi.net.type.Api;
import pixlze.guildapi.utils.McUtils;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedList;

public class WynnApiManager extends Api {
    public JsonObject wynnPlayerInfo;
    private boolean reloading = false;

    protected WynnApiManager() {
        super("wynn", new LinkedList<>());
    }

    public void initWynnPlayerInfo(boolean print) {
        new Thread(() -> {
            if (McUtils.mc().player != null) {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://api.wynncraft.com/v3/player/" + "pixlze"))
//                            .uri(URI.create("https://api.wynncraft.com/v3/player/" + McUtils.mc().player.getUuidAsString()))
                            .build();

                    HttpResponse<String> response = ApiManager.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                    GuildApi.LOGGER.info("wynn response: {}", response.body());
                    wynnPlayerInfo = GuildApi.gson.fromJson(response.body(), JsonObject.class);
                    if (wynnPlayerInfo.get("Error") != null) {
                        String message = wynnPlayerInfo.get("Error").getAsString();
                        wynnPlayerInfo = null;
                        throw new Exception(message);
                    }
                    GuildApi.LOGGER.info("successfully loaded wynn player info");
                    if (print)
                        McUtils.sendLocalMessage(Text.literal("Success!").setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
                    WynnApiEvents.SUCCESS.invoker().interact();
                } catch (Exception e) {
                    GuildApi.LOGGER.error("wynn player load error: {} {}", e, e.getMessage());
                    Managers.Api.apiCrash(
                            Text.literal("Wynn api fetch for " + McUtils.playerName() + " failed. Click ").setStyle(Style.EMPTY.withColor(Formatting.RED))
                                    .append(Text.literal("here").setStyle(Style.EMPTY.withUnderline(true).withColor(Formatting.RED)
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/reloadWynnInfo"))))
                                    .append(Text.literal(" to retry.").setStyle(Style.EMPTY.withColor(Formatting.RED))),
                            this);
                }
            } else {
                GuildApi.LOGGER.warn("null player found when initializing wynn api");
            }
        }).start();
    }

    private void onWynnJoin() {
        if (wynnPlayerInfo == null) {
            initWynnPlayerInfo(false);
        } else {
            GuildApi.LOGGER.warn("wynn player already initialized");
        }
    }

    @Override
    public void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("reloadWynnInfo").executes(context -> {
                if (!reloading) {
                    new Thread(() -> {
                        reloading = true;
                        McUtils.sendLocalMessage(Text.literal("Reloading...").setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
                        crashed = false;
                        enabled = true;
                        initWynnPlayerInfo(true);
                        reloading = false;
                    }).start();
                }
                return 0;
            }));
        });
        WynncraftConnectionEvents.JOIN.register(this::onWynnJoin);
        super.init();
    }
}
