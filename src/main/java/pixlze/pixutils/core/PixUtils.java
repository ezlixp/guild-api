package pixlze.pixutils.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Pair;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pixlze.pixutils.config.PixUtilsConfig;
import pixlze.pixutils.core.net.ApiManager;
import pixlze.pixutils.core.type_adapters.PairAdapter;
import pixlze.pixutils.core.type_adapters.PatternAdapter;
import pixlze.pixutils.features.chat_notifications.ChatNotifications;
import pixlze.pixutils.features.copy_chat.CopyChat;
import pixlze.pixutils.screens.config.PixUtilsConfigScreen;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.regex.Pattern;


public class PixUtils implements ClientModInitializer {
    public static final String MOD_ID = "pixutils";
    public static final Logger LOGGER = LoggerFactory.getLogger("pixutils");
    public static final HttpClient httpClient = HttpClientBuilder.create().build();
    public static Gson gson;
    public static KeyBinding openConfigKeybind;
    public static JsonObject secrets;

    @Override
    public void onInitializeClient() {
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

        ApiManager.initialize();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openConfigKeybind.wasPressed()) {
                client.setScreen(new PixUtilsConfigScreen(MinecraftClient.getInstance().currentScreen));
            }
            if (ApiManager.guildRaidTokenCreatedOn != null && new Date().getTime() - ApiManager.guildRaidTokenCreatedOn.getTime() >= 72000000) {
                PixUtils.LOGGER.info("refreshing token");
                ApiManager.refreshGuildRaidServerToken();
            }
        });


        PixUtilsConfig.initialize();
        CopyChat.initialize();
        ChatNotifications.initialize();
    }
}