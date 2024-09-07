package pixlze.guildapi.net;

import com.google.gson.JsonObject;
import net.minecraft.text.Text;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.mod.event.WynncraftConnectionEvents;
import pixlze.guildapi.net.type.Api;
import pixlze.guildapi.utils.McUtils;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedList;

public class WynnApiManager extends Api {
    public JsonObject wynnPlayerInfo;

    protected WynnApiManager() {
        super("wynn", new LinkedList<>());
    }

    public void initWynnPlayerInfo() {
        new Thread(() -> {
            if (McUtils.mc().player != null) {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
//                        .uri(URI.create("https://api.wynncraft.com/v3/player/" + "pixlze"))
                            .uri(URI.create("https://api.wynncraft.com/v3/player/" + McUtils.mc().player.getUuidAsString()))
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
                } catch (Exception e) {
                    GuildApi.LOGGER.error("wynn player load error: {} {}", e, e.getMessage());
                    Managers.Api.apiError(Text.of("wynn api error"), this, true);
                }
            } else {
                GuildApi.LOGGER.warn("null player found when initializing wynn api");
            }
        }).start();
    }

    private void onWynnJoin() {
        if (wynnPlayerInfo == null) {
            initWynnPlayerInfo();
        } else {
            GuildApi.LOGGER.warn("wynn player already initialized");
        }
    }

    @Override
    public void init() {
        WynncraftConnectionEvents.JOIN.register(this::onWynnJoin);
        super.init();
    }
}
