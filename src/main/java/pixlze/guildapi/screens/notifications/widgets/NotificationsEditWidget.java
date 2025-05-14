package pixlze.guildapi.screens.notifications.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import pixlze.guildapi.screens.notifications.NotificationsEditScreen;
import pixlze.guildapi.utils.McUtils;

import java.util.List;

public class NotificationsEditWidget extends ElementListWidget<NotificationsEditWidget.Entry> {
    public NotificationsEditWidget(MinecraftClient client, int width, NotificationsEditScreen notificationsScreen) {
        super(client, width, notificationsScreen.layout.getContentHeight(), notificationsScreen.layout.getHeaderHeight(), 25);
    }

    public void addNotification() {
        this.addEntry(Entry.create());
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
        private final TextFieldWidget regex;
        private final TextFieldWidget display;

        private Entry() {
            regex = new TextFieldWidget(McUtils.mc().textRenderer, 210, 0, Text.literal("Notification Regex"));
            display = new TextFieldWidget(McUtils.mc().textRenderer, 210, 0, Text.literal("Notification Display"));
        }

        public static Entry create() {
            return new Entry();
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return children();
        }

        @Override
        public List<? extends TextFieldWidget> children() {
            return List.of(regex, display);
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            regex.setHeight(entryHeight);
            display.setHeight(entryHeight);
            regex.setPosition(x + 20, y);
            display.setPosition(x + entryWidth - 20 - 210, y);

            regex.render(context, mouseX, mouseY, tickDelta);
            display.render(context, mouseX, mouseY, tickDelta);
        }
    }
}
