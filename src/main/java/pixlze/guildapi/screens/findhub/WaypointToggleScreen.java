package pixlze.guildapi.screens.findhub;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.core.config.AbstractConfigEditScreen;
import pixlze.guildapi.core.config.Config;
import pixlze.guildapi.core.waypoints.Waypoint;
import pixlze.guildapi.screens.findhub.widgets.WaypointsToggleWidget;
import pixlze.guildapi.utils.McUtils;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

public class WaypointToggleScreen extends AbstractConfigEditScreen<HashSet<String>> {
    public final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    private WaypointsToggleWidget body;
    private ButtonWidget allOnButton;

    public WaypointToggleScreen(Screen parent, Config<HashSet<String>> myConfig) {
        super("Waypoints", parent, myConfig);
        allOnButton = ButtonWidget.builder(Text.literal("Enable All"), (button) -> {
            this.body.setAllEntries(true);
        }).size(75, 25 - 4).build();
    }

    @Override
    public void init() {
        this.initHeader();
        this.initBody();
        this.initFooter();
        this.layout.forEachChild(this::addDrawableChild);
        this.addDrawableChild(allOnButton);
        this.refreshWidgetPositions();
        this.addWaypoints();
    }

    @Override
    protected void fillConfig() {
        HashSet<String> cur = new HashSet<>();
        for (String name : myConfig.getValue()) {
            cur.add(name.toLowerCase(Locale.ROOT));
        }
        for (Map.Entry<String, Waypoint> waypointEntry : Managers.Waypoint.getWaypoints().entrySet()) {
            if (waypointEntry.getValue().isActive()) {
                cur.remove(waypointEntry.getKey().toLowerCase());
            } else {
                cur.add(waypointEntry.getKey().toLowerCase());
            }
        }

        myConfig.setPending(cur);
    }

    private void addWaypoints() {
        for (Map.Entry<String, Waypoint> waypointEntry : Managers.Waypoint.getWaypoints().entrySet()) {
            body.addWaypoint(waypointEntry.getValue());
        }
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
        this.body = this.layout.addBody(new WaypointsToggleWidget(McUtils.mc(), this.width, this));
    }

    protected void initFooter() {
        DirectionalLayoutWidget directionalLayoutWidget = this.layout.addFooter(DirectionalLayoutWidget.horizontal()
                .spacing(8));
        directionalLayoutWidget.add(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close()).build());
    }

    @Override
    protected void refreshWidgetPositions() {
        this.layout.refreshPositions();
        this.allOnButton.setPosition(this.width - 75 - 6, 6);
        if (this.body != null)
            this.body.position(this.width, this.layout);
    }
}
