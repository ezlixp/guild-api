package pixlze.guildapi.screens.notifications.widgets;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import pixlze.guildapi.core.notifications.Notification;
import pixlze.guildapi.core.notifications.Trigger;
import pixlze.guildapi.screens.widgets.AllowSectionSignTextField;
import pixlze.guildapi.utils.McUtils;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class NotificationWidget extends ContainerWidget {
    private Element focused;
    private final AllowSectionSignTextField regex;
    private final AllowSectionSignTextField display;
    private final ButtonWidget remove;
    private final NotificationsEditListWidget widget;

    private NotificationWidget(NotificationsEditListWidget widget, String regex, String display, int width, int height, Text text) {
        super(0, 0, width, height, text);
        this.widget = widget;

        this.regex = new AllowSectionSignTextField(McUtils.mc().textRenderer, 125, height, Text.literal("Notification Regex"));
        this.display = new AllowSectionSignTextField(McUtils.mc().textRenderer, 125, height, Text.literal("Notification Display"));
        this.remove = ButtonWidget.builder(Text.literal("Remove"), button -> this.widget.removeNotification(button.getX() + (double) button.getWidth() / 2, button.getY() + (double) button.getHeight() / 2)).size(100, height).build();

        this.regex.setMaxLength(256);
        this.display.setMaxLength(100);
        this.regex.write(regex);
        this.display.write(display);
        this.regex.setPlaceholder(Text.literal("§7Regex"));
        this.display.setPlaceholder(Text.literal("§7Display"));
        this.regex.setDrawsBackground(false);
        this.display.setDrawsBackground(false);
    }

    private NotificationWidget(NotificationsEditListWidget widget) {
        this(widget, "", "");
    }

    private NotificationWidget(NotificationsEditListWidget widget, String regex, String display) {
        this(widget, regex, display, 400, 21, Text.literal("Notification"));
    }

    public static NotificationWidget of(NotificationsEditListWidget widget) {
        return new NotificationWidget(widget);
    }

    public static NotificationWidget of(NotificationsEditListWidget widget, Notification<Trigger.CHAT> notification) {
        return new NotificationWidget(widget, notification.trigger.toString(), notification.displayText);
    }

    @Nullable
    public Notification<Trigger.CHAT> getNotification() {
        if (regex.getText().isBlank() && display.getText().isBlank()) return null;
        return Notification.ofChat(regex.getText(), display.getText());
    }

    @Override
    public List<ClickableWidget> children() {
        return List.of(regex, display, remove);
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
        context.fill(this.regex.getX() - 4, this.remove.getY() - 2, this.remove.getRight() + 4, this.remove.getY() + this.remove.getHeight() + 2, 0xAA000000);

        this.regex.setPosition(this.getX() + 4, this.getY() + (this.height - 10) / 2);
        this.remove.setPosition(this.getRight() - this.remove.getWidth() - 4, this.getY());
        this.display.setPosition((this.regex.getX() + this.remove.getX()) / 2, this.getY() + (this.height - 10) / 2);

        try {
            Pattern.compile(this.regex.getText());
        } catch (PatternSyntaxException e) {
            context.drawTooltip(McUtils.mc().textRenderer, Text.literal("§cInvalid Regex."), this.regex.getX() + this.regex.getWidth() / 2 - 4, this.regex.getY() + this.regex.getHeight() + 4);
        }


        this.regex.render(context, mouseX, mouseY, delta);
        this.display.render(context, mouseX, mouseY, delta);
        this.remove.render(context, mouseX, mouseY, delta);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }

    @Override
    protected int getContentsHeightWithPadding() {
        return 0;
    }

    @Override
    protected double getDeltaYPerScroll() {
        return 0;
    }
}
