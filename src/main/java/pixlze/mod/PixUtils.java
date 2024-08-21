package pixlze.mod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pixlze.mod.config.PixUtilsConfigScreen;
import pixlze.mod.features.chat_regex.ChatNotifications;
import pixlze.mod.features.copy_chat.CopyChat;

public class PixUtils implements ModInitializer {
    public static final String MOD_ID = "pixutils";
    public static final Logger LOGGER = LoggerFactory.getLogger("pixutils");
    public static final Identifier CHAT_MESSAGE_PACKET_ID = Identifier.of(MOD_ID, "chat_message");
    public static KeyBinding openConfigKeybind;

    @Override
    public void onInitialize() {
        System.setProperty("java.awt.headless", "false");
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
        CopyChat.initialize();
        ChatNotifications.initialize();
    }
}