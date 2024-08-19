package pixlze.mod.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget.Builder;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import pixlze.mod.PixUtils;
import pixlze.mod.config.types.Option;
import pixlze.mod.config.types.SubConfig;
import pixlze.mod.config.types.Toggle;

public class PixUtilsConfigScreen extends Screen {

    static final private Identifier buttonTexture = Identifier.of(PixUtils.MOD_ID, "funni");
    static final private Identifier buttonTexture2 = Identifier.of(PixUtils.MOD_ID, "ahmeskeyoo.jpg");
    private final Screen parent;

    public PixUtilsConfigScreen(Screen parent) {
        super(Text.of("My Mod Config"));
        this.parent = parent;
    }

    private static @NotNull ToggleButtonWidget getToggleButtonWidget(Toggle option, int x, int y, int width, int height) {
        ButtonTextures textures = new ButtonTextures(buttonTexture, buttonTexture2, buttonTexture, buttonTexture2);
        ToggleButtonWidget button = new ToggleButtonWidget(x, y, width, height, option.getState()) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                super.setToggled(!super.toggled);
                option.setState(super.toggled);
                System.out.println("togglers");
            }
        };
        button.setTextures(textures);
        return button;
    }

    @Override
    protected void init() {
        for (Option option : PixUtilsConfig.getOptions()) {
            switch (option.type) {
                case "Toggle":
                    ToggleButtonWidget button = getToggleButtonWidget((Toggle) option, 200, 200, 64, 64);
                    this.addDrawableChild(button);
                    break;
                case "SubConfig":
                    Builder openSubConfigBuilder = new Builder(Text.of(((SubConfig<?>) option).buttonText), b -> {
                        ((SubConfig<?>) option).click();
                    });
                    openSubConfigBuilder.dimensions(this.width / 2 - 100, 20, 200, 20);
                    this.addDrawableChild(openSubConfigBuilder.build());
            }
        }
        Builder doneButtonBuilder = new Builder(Text.of("Done"), b -> {
            MinecraftClient.getInstance().setScreen(parent);
        });
        doneButtonBuilder.dimensions(this.width / 2 - 100, this.height - 50, 200, 20);
        this.addDrawableChild(doneButtonBuilder.build());
    }
}