package pixlze.guildapi.screens.config;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class ConfigScreen extends Screen {
    private final Screen parent;
    private int y = 0;
    public final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    private FeatureConfigListWidget body;

    public ConfigScreen(Screen parent) {
        super(Text.of("Config"));
        this.parent = parent;
    }

    @Override
    public void init() {
        this.initHeader();
        this.initBody();
        this.initFooter();
        this.layout.forEachChild(this::addDrawableChild);
        this.refreshWidgetPositions();
    }

    @Override
    public void close() {
        assert client != null;
        client.setScreen(parent);
    }

    protected void initHeader() {
        this.layout.addHeader(this.title, this.textRenderer);
    }

    protected void initBody() {
        this.body = this.layout.addBody(new FeatureConfigListWidget(this.client, this, this.width));
    }

    protected void initFooter() {
        this.layout.addFooter(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close()).width(200).build());
    }

    protected void refreshWidgetPositions() {
        this.layout.refreshPositions();
        if (this.body != null) {
            this.body.position(this.width, this.layout);
        }
    }
}
