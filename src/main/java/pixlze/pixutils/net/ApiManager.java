package pixlze.pixutils.net;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import pixlze.pixutils.PixUtils;
import pixlze.pixutils.net.models.GetTokenModel;

import java.util.Date;

public class ApiManager {
    public static JsonObject wynnPlayerInfo;
    public static String guildRaidServerToken;
    public static Date guildRaidTokenCreatedOn;

    public static void initialize() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            guildRaidTokenCreatedOn = new Date();
            new Thread(() -> {
                if (client.player != null && wynnPlayerInfo == null) {
                    HttpGet get = new HttpGet("https://api.wynncraft.com/v3/player/" + client.player.getUuidAsString());
//                    HttpGet get = new HttpGet("https://api.wynncraft.com/v3/player/" + "pixlze");
                    try {
                        HttpResponse response = PixUtils.httpClient.execute(get);
                        wynnPlayerInfo = PixUtils.gson.fromJson(EntityUtils.toString(response.getEntity()), JsonObject.class);
                        if (wynnPlayerInfo.get("Error") != null) {
                            String message = wynnPlayerInfo.get("Error").getAsString();
                            wynnPlayerInfo = null;
                            throw new Exception(message);
                        }
                        PixUtils.LOGGER.info("successfully loaded wynn player info");
                    } catch (Exception e) {
                        PixUtils.LOGGER.error("wynn player load error: {}", e.getMessage());
                    }
                } else {
                    PixUtils.LOGGER.warn("null player or already initialized wynn player info");
                }

                if (wynnPlayerInfo != null && guildRaidServerToken == null) {
                    try {
                        HttpPost post = new HttpPost(PixUtils.secrets.get("guild_raid_urls").getAsJsonObject().get(wynnPlayerInfo.get("guild").getAsJsonObject().get("prefix").getAsString()).getAsString() + "auth/getToken");
                        StringEntity body = new StringEntity(PixUtils.gson.toJson(new GetTokenModel(PixUtils.secrets.get("validation_key").getAsString())));
                        post.setEntity(body);
                        post.setHeader("Content-type", "application/json");
                        JsonObject response = PixUtils.gson.fromJson(EntityUtils.toString(PixUtils.httpClient.execute(post).getEntity()), JsonObject.class);
                        if (response.get("status").getAsBoolean()) {
                            guildRaidServerToken = response.get("token").getAsString();
                            PixUtils.LOGGER.info("successfully loaded guild raid server token");
                        } else {
                            PixUtils.LOGGER.error("Couldn't generate token with error: {}", response.get("error"));
                        }
                    } catch (Exception e) {
                        PixUtils.LOGGER.error("get token error: {}", e.getMessage());
                    }
                } else {
                    PixUtils.LOGGER.warn("wynn player info not initialized or guild raid server token already initialized");
                }
            }).start();
        });
    }

    public static void refreshGuildRaidServerToken() {
        guildRaidTokenCreatedOn = new Date();
        new Thread(() -> {
            if (wynnPlayerInfo != null) {
                try {
                    HttpPost post = new HttpPost(PixUtils.secrets.get("guild_raid_urls").getAsJsonObject().get(wynnPlayerInfo.get("guild").getAsJsonObject().get("prefix").getAsString()).getAsString() + "auth/getToken");
                    StringEntity body = new StringEntity(PixUtils.gson.toJson(new GetTokenModel(PixUtils.secrets.get("validation_key").getAsString())));
                    post.setEntity(body);
                    post.setHeader("Content-type", "application/json");
                    JsonObject response = PixUtils.gson.fromJson(EntityUtils.toString(PixUtils.httpClient.execute(post).getEntity()), JsonObject.class);
                    if (response.get("status").getAsBoolean()) {
                        guildRaidServerToken = response.get("token").getAsString();
                        PixUtils.LOGGER.info("successfully refreshed guild raid server token");
                    } else {
                        PixUtils.LOGGER.error("Couldn't refresh token with error: {}", response.get("error"));
                    }
                } catch (Exception e) {
                    PixUtils.LOGGER.error("refresh token error: {}", e.getMessage());
                }
            } else {
                PixUtils.LOGGER.warn("wynn player not initialized, can't refresh token");
            }
        }).start();
    }
}