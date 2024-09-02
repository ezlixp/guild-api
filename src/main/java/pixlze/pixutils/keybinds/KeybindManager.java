package pixlze.pixutils.keybinds;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import pixlze.pixutils.screens.config.PixUtilsConfigScreen;

public class KeybindManager {
    public static KeyBinding openConfigKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "Open Config",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_END,
            "Pix Utils"
    ));

    public void checkKeybinds(MinecraftClient client) {
        if (openConfigKeybind.wasPressed()) {
            client.setScreen(new PixUtilsConfigScreen(MinecraftClient.getInstance().currentScreen));
        }
    }
}
