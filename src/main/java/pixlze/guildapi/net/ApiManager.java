package pixlze.guildapi.net;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.net.models.GetTokenModel;

import java.util.Date;

public class ApiManager {
    public static final HttpClient httpClient = HttpClientBuilder.create().build();
    public static JsonObject wynnPlayerInfo; // put in cache
    public static String guildRaidServerToken; // put in cache
    public static Date guildRaidTokenCreatedOn; // refresh as needed

    // create functions for: getting specific data from player from api, making call to guild raid server (one for each endpoint)
    // add crashing (if crash, disable features)
    public static void init() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            guildRaidTokenCreatedOn = new Date();
            new Thread(() -> {
                if (client.player != null && wynnPlayerInfo == null) {
                    HttpGet get = new HttpGet("https://api.wynncraft.com/v3/player/" + client.player.getUuidAsString());
//                    HttpGet get = new HttpGet("https://api.wynncraft.com/v3/player/" + "pixlze");
                    try {
                        HttpResponse response = httpClient.execute(get);
                        wynnPlayerInfo = GuildApi.gson.fromJson(EntityUtils.toString(response.getEntity()), JsonObject.class);
                        if (wynnPlayerInfo.get("Error") != null) {
                            String message = wynnPlayerInfo.get("Error").getAsString();
                            wynnPlayerInfo = null;
                            throw new Exception(message);
                        }
                        GuildApi.LOGGER.info("successfully loaded wynn player info");
                    } catch (Exception e) {
                        GuildApi.LOGGER.error("wynn player load error: {}", e.getMessage());
                    }
                } else {
                    GuildApi.LOGGER.warn("null player or already initialized wynn player info");
                }

                if (wynnPlayerInfo != null && guildRaidServerToken == null) {
                    try {
                        HttpPost post = new HttpPost(GuildApi.secrets.get("guild_raid_urls").getAsJsonObject().get(wynnPlayerInfo.get("guild").getAsJsonObject().get("prefix").getAsString()).getAsString() + "auth/getToken");
                        StringEntity body = new StringEntity(GuildApi.gson.toJson(new GetTokenModel(GuildApi.secrets.get("validation_key").getAsString())));
                        post.setEntity(body);
                        post.setHeader("Content-type", "application/json");
                        JsonObject response = GuildApi.gson.fromJson(EntityUtils.toString(httpClient.execute(post).getEntity()), JsonObject.class);
                        if (response.get("status").getAsBoolean()) {
                            guildRaidServerToken = response.get("token").getAsString();
                            GuildApi.LOGGER.info("successfully loaded guild raid server token");
                        } else {
                            GuildApi.LOGGER.error("Couldn't generate token with error: {}", response.get("error"));
                        }
                    } catch (Exception e) {
                        GuildApi.LOGGER.error("get token error: {}", e.getMessage());
                    }
                } else {
                    GuildApi.LOGGER.warn("wynn player info not initialized or guild raid server token already initialized");
                }
            }).start();
        });
    }

    public static void refreshGuildRaidServerToken() {
        guildRaidTokenCreatedOn = new Date();
        new Thread(() -> {
            if (wynnPlayerInfo != null) {
                try {
                    HttpPost post = new HttpPost(GuildApi.secrets.get("guild_raid_urls").getAsJsonObject().get(wynnPlayerInfo.get("guild").getAsJsonObject().get("prefix").getAsString()).getAsString() + "auth/getToken");
                    StringEntity body = new StringEntity(GuildApi.gson.toJson(new GetTokenModel(GuildApi.secrets.get("validation_key").getAsString())));
                    post.setEntity(body);
                    post.setHeader("Content-type", "application/json");
                    JsonObject response = GuildApi.gson.fromJson(EntityUtils.toString(httpClient.execute(post).getEntity()), JsonObject.class);
                    if (response.get("status").getAsBoolean()) {
                        guildRaidServerToken = response.get("token").getAsString();
                        GuildApi.LOGGER.info("successfully refreshed guild raid server token");
                    } else {
                        GuildApi.LOGGER.error("Couldn't refresh token with error: {}", response.get("error"));
                    }
                } catch (Exception e) {
                    GuildApi.LOGGER.error("refresh token error: {}", e.getMessage());
                }
            } else {
                GuildApi.LOGGER.warn("wynn player not initialized, can't refresh token");
            }
        }).start();
    }
}
