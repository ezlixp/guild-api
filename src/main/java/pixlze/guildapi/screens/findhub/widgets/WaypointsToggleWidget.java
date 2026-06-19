package pixlze.guildapi.screens.findhub.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import pixlze.guildapi.core.waypoints.Waypoint;
import pixlze.guildapi.screens.findhub.WaypointToggleScreen;
import pixlze.guildapi.utils.McUtils;

import java.util.List;

public class WaypointsToggleWidget extends ElementListWidget<WaypointsToggleWidget.Entry> {
    public WaypointsToggleWidget(MinecraftClient client, int width, WaypointToggleScreen waypointsScreen) {
        super(client, width, waypointsScreen.layout.getContentHeight(), waypointsScreen.layout.getHeaderHeight(), 29);
    }

    public void addWaypoint(Waypoint waypoint) {
        this.addEntry(new WaypointsToggleWidget.Entry(waypoint));
    }

    @Override
    public int getRowLeft() {
        return this.getX();
    }

    @Override
    public int getRowRight() {
        return this.getRowLeft() + this.getRowWidth() - (this.overflows() ? 6:0);
    }

    @Override
    public int getRowWidth() {
        return this.width;
    }

    @Override
    protected int getScrollbarX() {
        return this.getRight() - 6;
    }

    public void setAllEntries(boolean to) {
        for (WaypointsToggleWidget.Entry entry : this.children()) {
            entry.setActive(to);
        }
    }

    public static class Entry extends ElementListWidget.Entry<Entry> implements ParentElement {
        private final Waypoint tracked;
        private final TextWidget name;
        private final ButtonWidget toggle;

        public Entry(Waypoint tracked) {
            this.tracked = tracked;
            name = new TextWidget(100, 25 - 4, Text.literal(tracked.getUsername()), McUtils.mc().textRenderer);
            toggle = ButtonWidget.builder(Text.of(tracked.isActive() ? "On":"Off"), (button) -> {
                        this.setActive(!tracked.isActive());
                    }).dimensions(0, 0, 100, 25 - 4)
                    .build();
        }

        public void setActive(boolean active) {
            toggle.setMessage(Text.of(active ? "On":"Off"));
            tracked.setActive(active);
        }

        @Nullable
        public Waypoint getTracked() {
            return tracked;
        }

        @Override
        public void setFocused(@Nullable Element focused) {
            super.setFocused(focused);
            if (focused == null) {
                name.setFocused(false);
                toggle.setFocused(false);
            }
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return children();
        }

        @Override
        public List<ClickableWidget> children() {
            return List.of(name, toggle);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            name.setPosition(10, getY());
            toggle.setPosition(getX() + getWidth() - toggle.getWidth() - 10, getY());

            name.render(context, mouseX, mouseY, tickDelta);
            toggle.render(context, mouseX, mouseY, tickDelta);
        }
    }
}
