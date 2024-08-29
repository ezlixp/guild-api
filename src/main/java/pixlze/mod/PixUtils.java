package pixlze.mod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Pair;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pixlze.mod.config.PixUtilsConfig;
import pixlze.mod.config.PixUtilsConfigScreen;
import pixlze.mod.features.chat_notifications.ChatNotifications;
import pixlze.mod.features.copy_chat.CopyChat;
import pixlze.mod.type_adapters.PairAdapter;
import pixlze.mod.type_adapters.PatternAdapter;
import pixlze.utils.requests.GetTokenPojo;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;


public class PixUtils implements ModInitializer {
    public static final String MOD_ID = "pixutils";
    public static final Logger LOGGER = LoggerFactory.getLogger("pixutils");
    public static final HttpClient httpClient = HttpClientBuilder.create().build();
    public static Gson gson;
    public static KeyBinding openConfigKeybind;
    public static String guildRaidServerToken;
    public static JsonObject wynnPlayerInfo;
    public static JsonObject secrets;

    @Override
    public void onInitialize() {
        System.setProperty("java.awt.headless", "false");

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Pattern.class, new PatternAdapter().nullSafe());
        builder.registerTypeAdapter(Pair.class, new PairAdapter().nullSafe());
        gson = builder.create();

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("secrets.json");
        if (inputStream == null) {
            PixUtils.LOGGER.error("secret variables could not be loaded");
        } else {
            secrets = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
        }


        openConfigKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Open Config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_END,
                "Pix Utils"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openConfigKeybind.wasPressed()) {
                client.setScreen(new PixUtilsConfigScreen(MinecraftClient.getInstance().currentScreen));
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> new Thread(() -> {
            if (client.player != null && wynnPlayerInfo == null) {
                HttpGet get = new HttpGet("https://api.wynncraft.com/v3/player/" + client.player.getUuidAsString());
                try {
                    HttpResponse response = httpClient.execute(get);
                    wynnPlayerInfo = gson.fromJson(EntityUtils.toString(response.getEntity()), JsonObject.class);
                    if (wynnPlayerInfo.get("Error") != null) {
                        String message = wynnPlayerInfo.get("Error").getAsString();
                        wynnPlayerInfo = null;
                        throw new Exception(message);
                    }
                    PixUtils.LOGGER.info("successfully loaded wynn player info");
                    PixUtils.LOGGER.info(wynnPlayerInfo.toString());
                } catch (Exception e) {
                    PixUtils.LOGGER.error("wynn player load error: {}", e.getMessage());
                }
            } else {
                PixUtils.LOGGER.warn("null player or already initialized wynn player info");
            }
            if (wynnPlayerInfo != null && PixUtils.guildRaidServerToken == null) {
                HttpPost post = new HttpPost(PixUtils.secrets.get("guild_raid_urls").getAsJsonObject().get(wynnPlayerInfo.get("guild").getAsJsonObject().get("prefix").getAsString()).getAsString() + "auth/getToken");
                try {
                    StringEntity body = new StringEntity(PixUtils.gson.toJson(new GetTokenPojo(secrets.get("validation_key").getAsString())));
                    post.setEntity(body);
                    post.setHeader("Content-type", "application/json");
                    JsonObject response = gson.fromJson(EntityUtils.toString(httpClient.execute(post).getEntity()), JsonObject.class);
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
        }).start());

        PixUtilsConfig.init();
        CopyChat.initialize();
        ChatNotifications.initialize();
    }
}