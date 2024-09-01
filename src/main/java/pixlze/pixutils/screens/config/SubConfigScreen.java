package pixlze.pixutils.screens.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class SubConfigScreen extends Screen {
    protected Screen parent;

    protected SubConfigScreen(Text title) {
        super(title);
    }

    public void open(Screen parent) {
        this.parent = parent;
        MinecraftClient.getInstance().setScreen(this);
    }
}
