package pixlze.guildapi.screens.menu;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.text.Text;
import pixlze.guildapi.screens.config.ConfigScreen;
import pixlze.guildapi.screens.discord.DiscordScreen;
import pixlze.guildapi.screens.menu.widgets.MenuOptionsListWidget;
import pixlze.guildapi.screens.widgets.FontSizeTextWidget;
import pixlze.guildapi.utils.McUtils;

public class MenuScreen extends Screen {
    private final Screen parent;
    public final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this, 0, 0);
    private MenuOptionsListWidget body;

    public MenuScreen(Screen parent) {
        super(Text.of("Config"));
        this.parent = parent;
    }


    @Override
    public void init() {
        this.initHeader();
        this.initBody();
        this.layout.forEachChild(this::addDrawableChild);
        this.refreshWidgetPositions();
    }

    @Override
    public void close() {
        assert client != null;
        client.setScreen(parent);
    }

    protected void initHeader() {
        this.layout.addHeader(new FontSizeTextWidget(75, Text.of("Guild Api"), this.textRenderer));
    }

    protected void initBody() {
        this.body = this.layout.addBody(new MenuOptionsListWidget(McUtils.mc(), this.width, this));
        this.addOptions();
    }

    private void addOptions() {
        this.body.addOption(new ConfigScreen(this));
        this.body.addOption(new DiscordScreen(this));
    }

    @Override
    protected void refreshWidgetPositions() {
        this.layout.setHeaderHeight(this.height / 3);
        this.layout.forEachChild((child) -> child.setHeight(this.height / 4));
        this.layout.refreshPositions();
        if (this.body != null) {
            this.body.position(this.width, this.layout);
        }
    }
}
