package pixlze.guildapi.screens.notifications.widgets;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.notifications.Notification;
import pixlze.guildapi.core.notifications.NotificationTrigger;
import pixlze.guildapi.utils.McUtils;

import java.util.List;

public class NotificationWidget extends ClickableWidget implements ParentElement {
    private Element focused;
    private boolean dragging = false;
    private final TextFieldWidget regex;
    private final TextFieldWidget display;
    private final ButtonWidget remove;
    private final NotificationsEditWidget widget;

    private NotificationWidget(NotificationsEditWidget widget, String regex, String display, int width, int height, Text text) {
        super(0, 0, width, height, text);
        this.widget = widget;

        this.regex = new TextFieldWidget(McUtils.mc().textRenderer, 150, height, Text.literal("Notification Regex"));
        this.display = new TextFieldWidget(McUtils.mc().textRenderer, 150, height, Text.literal("Notification Display"));
        this.remove = ButtonWidget.builder(Text.literal("Remove"), button -> {
            this.widget.removeNotification(button.getX() + (double) button.getWidth() / 2, button.getY() + (double) button.getHeight() / 2);
            GuildApi.LOGGER.info("hi");
        }).size(100, height).build();

        this.regex.write(regex);
        this.display.write(display);

    }

    private NotificationWidget(NotificationsEditWidget widget) {
        this(widget, "", "", 400, 21, Text.literal("Notification"));
    }

    public static NotificationWidget of(NotificationsEditWidget widget) {
        return new NotificationWidget(widget);
    }

    public static NotificationWidget of(NotificationsEditWidget widget, Notification<NotificationTrigger.CHAT> notification) {
        return new NotificationWidget(widget, notification.trigger.toString(), notification.displayText, 0, 0, Text.literal("Notification"));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return ParentElement.super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public List<ClickableWidget> children() {
        return List.of(regex, display, remove);
    }

    @Override
    public boolean isDragging() {
        return dragging;
    }

    @Override
    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    @Override
    public @Nullable Element getFocused() {
        return focused;
    }

    @Override
    public void setFocused(@Nullable Element focused) {
        if (this.focused != null) {
            this.focused.setFocused(false);
        }

        if (focused != null) {
            focused.setFocused(true);
        }

        this.focused = focused;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        this.regex.setPosition(this.getX(), this.getY());
        this.display.setPosition(this.getX() + this.regex.getWidth(), this.getY());
        this.remove.setPosition(this.getRight() - this.remove.getWidth(), this.getY());

        this.regex.render(context, mouseX, mouseY, delta);
        this.display.render(context, mouseX, mouseY, delta);
        this.remove.render(context, mouseX, mouseY, delta);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }
}
