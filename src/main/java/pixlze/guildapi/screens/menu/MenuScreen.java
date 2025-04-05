package pixlze.guildapi.screens.menu;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.text.Text;
import pixlze.guildapi.core.Managers;
import pixlze.guildapi.screens.config.widgets.FeatureConfigListWidget;
import pixlze.guildapi.screens.widgets.FontSizeTextWidget;

public class MenuScreen extends Screen {
    private final Screen parent;
    public final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this, this.height / 3, 0);
    private FeatureConfigListWidget body;

    public MenuScreen(Screen parent) {
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

    @Override
    public void removed() {
        Managers.Config.saveConfig();
    }

    protected void initHeader() {
        this.layout.addHeader(new FontSizeTextWidget(200, Text.of("Guild Api"), this.textRenderer));
    }

    protected void initBody() {
//        this.body = this.layout.addBody(new FeatureConfigListWidget(this));
    }

    protected void initFooter() {
    }

    @Override
    protected void refreshWidgetPositions() {
        this.layout.refreshPositions();
        if (this.body != null) {
            this.body.position(this.width, this.layout);
        }
    }
}
