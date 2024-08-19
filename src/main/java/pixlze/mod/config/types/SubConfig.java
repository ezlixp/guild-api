package pixlze.mod.config.types;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import pixlze.mod.features.chat_regex.EditNotificationsScreen;

import java.util.ArrayList;


public class SubConfig<T> extends Option {
    public final String buttonText;
    private final ArrayList<T> value;
    private final Screen open;

    public SubConfig(String name, String buttonText, Screen open, ArrayList<T> value) {
        super(name, "SubConfig");
        this.name = name;
        this.buttonText = buttonText;
        this.open = open;
        this.value = value;
    }

    public void click() {
        ((EditNotificationsScreen) open).open(MinecraftClient.getInstance().currentScreen);
    }

    public ArrayList<T> getValue() {
        return value;
    }
}
