package pixlze.guildapi.screens.notifications.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;
import org.jetbrains.annotations.Nullable;
import pixlze.guildapi.core.notifications.Notification;
import pixlze.guildapi.core.notifications.Trigger;
import pixlze.guildapi.screens.notifications.NotificationsEditScreen;

import java.util.List;
import java.util.Objects;

public class NotificationsEditWidget extends ElementListWidget<NotificationsEditWidget.Entry> {
    public NotificationsEditWidget(MinecraftClient client, int width, NotificationsEditScreen notificationsScreen) {
        super(client, width, notificationsScreen.layout.getContentHeight(), notificationsScreen.layout.getHeaderHeight(), 25);
    }

    public void addNotification() {
        this.addEntry(new Entry(this));
    }

    public void addNotification(Notification<Trigger.CHAT> notif) {
        this.addEntry(new Entry(this, notif));
    }

    public void removeNotification(double mouseX, double mouseY) {
        this.removeEntry(this.getEntryAtPosition(mouseX, mouseY));
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

    public static class Entry extends ElementListWidget.Entry<NotificationsEditWidget.Entry> implements ParentElement {
        private final NotificationsEditWidget parent;
        private final NotificationWidget widget;

        public Entry(NotificationsEditWidget parent) {
            this.parent = parent;
            widget = NotificationWidget.of(parent);
        }

        public Entry(NotificationsEditWidget parent, Notification<Trigger.CHAT> notif) {
            this.parent = parent;
            widget = NotificationWidget.of(parent, notif);
        }

        @Nullable
        public Notification<Trigger.CHAT> getNotification() {
            return widget.getNotification();
        }

        @Override
        public void setFocused(@Nullable Element focused) {
            super.setFocused(focused);
            if (focused == null) {
                widget.setFocused(false);
                widget.setFocused(null);
            }
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return children();
        }

        @Override
        public List<NotificationWidget> children() {
            return List.of(widget);
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            widget.setHeight(entryHeight);
            widget.setPosition(x + entryWidth / 2 - widget.getWidth() / 2, y);

            widget.render(context, mouseX, mouseY, tickDelta);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return Objects.equals(parent.getEntryAtPosition(mouseX, mouseY), this);
        }
    }
}
