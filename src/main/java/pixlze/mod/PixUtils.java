package pixlze.mod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pixlze.mod.config.PixUtilsConfig;
import pixlze.mod.config.PixUtilsConfigScreen;
import pixlze.mod.features.chat_regex.ChatNotifications;
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

        PixUtilsConfig.init();
        CopyChat.initialize();
        ChatNotifications.initialize();
    }
}