package pixlze.mod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;


public class PixUtils implements ModInitializer {
    public static final String MOD_ID = "pixutils";
    public static final Logger LOGGER = LoggerFactory.getLogger("pixutils");
    public static final StringVisitable.Visitor<String> plainVisitor = new StringVisitable.Visitor<String>() {
        @Override
        public Optional<String> accept(String asString) {
            LOGGER.info("{} visiting {}", MOD_ID, asString);
            return Optional.empty();
        }
    };
    public static final HttpClient httpClient = HttpClientBuilder.create().build();
    public static Gson gson;
    public static KeyBinding openConfigKeybind;
    public static String currentVisit;
    public static final StringVisitable.StyledVisitor<String> wynnVisitor = new StringVisitable.StyledVisitor<>() {
        @Override
        public Optional<String> accept(Style style, String asString) {
            if (style.getFont().getPath().startsWith("hud")) {
                return "break".describeConstable();
            }
            if (style.getColor() != null) {
                int colorIndex = 0;
                for (Formatting format : Formatting.values()) {
                    if (format.getColorValue() != null && format.getColorValue().equals(style.getColor().getRgb())) {
                        colorIndex = format.getColorIndex();
                        break;
                    }
                }
                currentVisit += "&" + Objects.requireNonNull(Formatting.byColorIndex(colorIndex)).getCode();
            }
            if (style.isBold()) {
                currentVisit += "&" + Formatting.BOLD.getCode();
            }
            if (style.isItalic()) {
                currentVisit += "&" + Formatting.ITALIC.getCode();
            }
            if (style.isUnderlined()) {
                currentVisit += "&" + Formatting.UNDERLINE.getCode();
            }
            if (style.isStrikethrough()) {
                currentVisit += "&" + Formatting.STRIKETHROUGH.getCode();
            }
            if (style.isObfuscated()) {
                currentVisit += "&" + Formatting.OBFUSCATED.getCode();
            }
            currentVisit += asString.replaceAll("\\n", "\\\\n");
            return Optional.empty();
        }
    };
    public static JsonObject wynnPlayerInfo;

    @Override
    public void onInitialize() {
        System.setProperty("java.awt.headless", "false");

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Pattern.class, new PatternAdapter().nullSafe());
        builder.registerTypeAdapter(Pair.class, new PairAdapter().nullSafe());
        gson = builder.create();

        openConfigKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Open Config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_END,
                "Pix Utils"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openConfigKeybind.wasPressed()) {
                MinecraftClient.getInstance().setScreen(new PixUtilsConfigScreen(MinecraftClient.getInstance().currentScreen));
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.getCurrentServerEntry() != null) {
                PixUtils.LOGGER.info(client.getCurrentServerEntry().address);
                if (client.getCurrentServerEntry().address.equals("play.wynncraft.com")) {
                    new Thread(() -> {
                        assert client.player != null;
                        HttpGet get = new HttpGet("https://api.wynncraft.com/v3/player/" + client.player.getUuidAsString());
                        try {
                            HttpResponse response = httpClient.execute(get);
                            wynnPlayerInfo = gson.fromJson(EntityUtils.toString(response.getEntity()), JsonObject.class);
                        } catch (Exception e) {
                            PixUtils.LOGGER.error("error: {}", e.getMessage());
                        }
                    }).start();
                }
            } else PixUtils.LOGGER.info("null server");
        });

        PixUtilsConfig.init();
        CopyChat.initialize();
        ChatNotifications.initialize();
    }
}