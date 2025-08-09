package pixlze.guildapi.screens.notifications;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.core.notifications.Notification;
import pixlze.guildapi.core.notifications.Trigger;
import pixlze.guildapi.screens.notifications.widgets.NotificationsEditListWidget;
import pixlze.guildapi.utils.McUtils;

public class NotificationsEditScreen extends Screen {
    private final Screen parent;
    private final ButtonWidget helpButton;
    public final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    private NotificationsEditListWidget body;

    public NotificationsEditScreen(Screen parent) {
        super(Text.of("Notifications"));
        this.parent = parent;
        helpButton = ButtonWidget.builder(Text.literal("?"), (button) -> {
            McUtils.mc().keyboard.setClipboard("§");
        }).size(20, 20).tooltip(Tooltip.of(Text.literal("The left box accepts regex, or regular expressions. Visit https://regexr.com/ to learn more. \nFor reference, the exact text of chat messages can be copied by alt-clicking on them. \n\nClick here to copy the formatting code."))).build();
    }

    @Override
    public void init() {
        this.initHeader();
        this.initBody();
        this.initFooter();
        this.layout.forEachChild(this::addDrawableChild);
        this.addDrawableChild(helpButton);
        this.refreshWidgetPositions();
        this.addNotifications();
    }

    private void addNotifications() {
        for (Notification<Trigger.CHAT> notification : Managers.Notification.getNotifications(Trigger.CHAT.class)) {
            body.addNotification(notification);
        }
    }

    @Override
    public void close() {
        assert client != null;
        client.setScreen(parent);
    }

    @Override
    public void removed() {
        Managers.Notification.saveNotifications(body.children());
    }

    protected void initHeader() {
        this.layout.addHeader(this.title, this.textRenderer);
    }

    protected void initBody() {
        this.body = this.layout.addBody(new NotificationsEditListWidget(McUtils.mc(), this.width, this));
    }

    protected void initFooter() {
        DirectionalLayoutWidget directionalLayoutWidget = this.layout.addFooter(DirectionalLayoutWidget.horizontal().spacing(8));
        directionalLayoutWidget.add(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close()).build());
        directionalLayoutWidget.add(ButtonWidget.builder(Text.literal("Add Notification"), button -> this.body.addNotification()).build());
    }

    @Override
    protected void refreshWidgetPositions() {
        this.layout.refreshPositions();
        this.helpButton.setPosition(this.width - 20 - 6, 6);
        if (this.body != null)
            this.body.position(this.width, this.layout);
    }
}
