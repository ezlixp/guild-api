package pixlze.mod.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget.Builder;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import pixlze.mod.PixUtils;
import pixlze.mod.config.types.Option;
import pixlze.mod.config.types.SubConfig;
import pixlze.mod.config.types.Toggle;
import pixlze.utils.gui.ClickableChild;
import pixlze.utils.gui.ScrollableContainer;

import java.io.IOException;

public class PixUtilsConfigScreen extends Screen {

    private static final Identifier buttonTexture = Identifier.of(PixUtils.MOD_ID, "funni");
    private static final Identifier buttonTexture2 = Identifier.of(PixUtils.MOD_ID, "null");
    private final Screen parent;
    private ScrollableContainer optionsContainer;
    private int rowY = 40;

    public PixUtilsConfigScreen(Screen parent) {
        super(Text.of("My Mod Config"));
        this.parent = parent;
    }

    private static @NotNull ToggleButtonWidget getToggleButtonWidget(Toggle option, int x, int y) {
        ButtonTextures textures = new ButtonTextures(buttonTexture, buttonTexture2, buttonTexture, buttonTexture2);
        ToggleButtonWidget button = new ToggleButtonWidget(x, y, 30, 20, option.getValue()) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                super.setToggled(!super.toggled);
                option.setValue(super.toggled);
            }
        };
        button.setTextures(textures);
        return button;
    }

    private void addOption(Option option) {
        TextWidget label = new TextWidget(50, rowY + 6, textRenderer.getWidth(option.name), textRenderer.fontHeight, Text.of(option.name), textRenderer);
        optionsContainer.addClickableChild(new ClickableChild<>(label));
        switch (option.type) {
            case "Toggle":
                ToggleButtonWidget toggle = getToggleButtonWidget((Toggle) option, width - 80, rowY);
                optionsContainer.addClickableChild(new ClickableChild<>(toggle));
                break;
            case "SubConfig":
                Builder openSubConfigBuilder = new Builder(Text.of(((SubConfig<?>) option).buttonText), b -> ((SubConfig<?>) option).click());
                openSubConfigBuilder.dimensions(width - 80, rowY, 30, 20);
                optionsContainer.addClickableChild(new ClickableChild<>(openSubConfigBuilder.build()));
                break;
        }
        rowY += 30;
    }

    @Override
    public void close() {
        try {
            PixUtilsConfig.save();
        } catch (IOException e) {
            PixUtils.LOGGER.error(e.getMessage());
        }
        PixUtils.LOGGER.info("config saved");
        super.close();
    }

    @Override
    protected void init() {
        Text message = Text.of("PixUtils Config");
        PixUtils.LOGGER.info("{}", textRenderer.getWidth(message));

        TextWidget configScreenLabel = new TextWidget(0, 12, width, textRenderer.fontHeight, message, textRenderer);

        optionsContainer = new ScrollableContainer(0, 30, width, height - 60, Text.of("mod options container"), 0.5F);

        Builder doneButtonBuilder = new Builder(Text.of("Done"), b -> {
            try {
                PixUtilsConfig.save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            MinecraftClient.getInstance().setScreen(parent);
        });
        doneButtonBuilder.dimensions(this.width / 2 - 100, this.height - 25, 200, 20);
        ButtonWidget doneButton = doneButtonBuilder.build();

        for (Option option : PixUtilsConfig.getOptions()) {
            addOption(option);
        }

        this.addDrawableChild(configScreenLabel);
        this.addDrawableChild(optionsContainer);
        this.addDrawableChild(doneButton);
    }
}