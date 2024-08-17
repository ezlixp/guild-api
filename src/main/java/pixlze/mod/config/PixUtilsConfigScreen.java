package pixlze.mod.config;

import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import pixlze.mod.PixUtils;
import pixlze.mod.config.buttons.ToggleButton;
import pixlze.mod.config.types.Option;
import pixlze.mod.config.types.Toggle;

public class PixUtilsConfigScreen extends Screen {

    static final private Identifier buttonTexture = Identifier.of(PixUtils.MOD_ID, "funni");
    static final private Identifier buttonTexture2 = Identifier.of(PixUtils.MOD_ID, "ahmeskeyoo.jpg");
    // private final Screen parent;
    // private PixUtilsConfig config;

    protected PixUtilsConfigScreen(Screen parent) {
        super(Text.of("My Mod Config"));
        // this.parent = parent;
        // this.config = new PixUtilsConfig();
    }

    @Override
    protected void init() {
        for (Option option : PixUtilsConfig.getOptions()) {
            if (option.type.equals("Toggle")) {
                System.out.println("innit");
                ButtonTextures textures = new ButtonTextures(buttonTexture, buttonTexture2, buttonTexture, buttonTexture2);
                ToggleButton button = new ToggleButton((Toggle) option, 200, 200, 64, 64, textures);
                this.addDrawableChild(button);

            }
        }
    }
}