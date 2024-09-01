package pixlze.pixutils.features.chat_notifications;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;
import pixlze.pixutils.PixUtils;
import pixlze.pixutils.config.types.SubConfigScreen;
import pixlze.pixutils.gui.ClickableChild;
import pixlze.pixutils.gui.ScrollableContainer;
import pixlze.pixutils.utils.ButtonWidgetUtils;

import java.util.ArrayList;
import java.util.regex.Pattern;


public class EditNotificationsScreen extends SubConfigScreen {
    protected final ArrayList<Pair<ClickableChild<TextFieldWidget>, ClickableChild<TextFieldWidget>>> notifications;
    protected final ArrayList<ClickableChild<ButtonWidget>> removeButtons;
    protected int rowY = 40;
    private ScrollableContainer inputContainer;


    public EditNotificationsScreen() {
        super(Text.of("Notifications"));
        notifications = new ArrayList<>();
        removeButtons = new ArrayList<>();
    }

    private void saveConfig() {
        ChatNotifications.config.getValue().clear();
        for (Pair<ClickableChild<TextFieldWidget>, ClickableChild<TextFieldWidget>> notif : notifications) {
            if (!notif.getLeft().getChild().getText().isEmpty() && !notif.getRight().getChild().getText().isEmpty()) {
                ChatNotifications.config.getValue().add(new Pair<>(Pattern.compile(notif.getLeft().getChild().getText()), notif.getRight().getChild().getText()));
            }
        }
    }

    private void removeNotification(ClickableChild<ButtonWidget> asChild) {
        int index = removeButtons.indexOf(asChild);
        inputContainer.queueRemove(notifications.get(index).getLeft());
        inputContainer.queueRemove(notifications.get(index).getRight());
        inputContainer.queueRemove(removeButtons.removeLast());

        notifications.remove(index);

        for (int i = index; i < notifications.size(); ++i) {
            notifications.get(i).getLeft().getChild().setY(notifications.get(i).getLeft().getY() - 30);
            notifications.get(i).getRight().getChild().setY(notifications.get(i).getRight().getY() - 30);
        }
        rowY -= 30;
    }

    private void addRow(@Nullable Pair<Pattern, String> notification) {
        TextFieldWidget regexInput = new TextFieldWidget(this.textRenderer, this.width / 3 + 10, rowY, 100, 20, Text.of("Input Regex"));
        regexInput.setEditable(true);
        regexInput.setPlaceholder(Text.of("Input Regex"));
        if (notification != null) regexInput.write(notification.getLeft().toString());
        ClickableChild<TextFieldWidget> regexInputClickable = new ClickableChild<>(regexInput);
        inputContainer.addClickableChild(regexInputClickable);

        TextFieldWidget notificationInput = new TextFieldWidget(this.textRenderer, this.width / 3 + 120, rowY, 100, 20, Text.of("Input Notification"));
        notificationInput.setEditable(true);
        notificationInput.setPlaceholder(Text.of("Input Notification"));
        if (notification != null) notificationInput.write(notification.getRight());
        ClickableChild<TextFieldWidget> notificationInputClickable = new ClickableChild<>(notificationInput);
        inputContainer.addClickableChild(notificationInputClickable);

        notifications.add(new Pair<>(regexInputClickable, notificationInputClickable));

        ButtonWidget removeButton = ButtonWidgetUtils.build(Text.of("Remove"), b -> removeNotification(new ClickableChild<>(b)), 2 * this.width / 3 - 60, rowY, 50, 20);
        ClickableChild<ButtonWidget> removeButtonClickable = new ClickableChild<>(removeButton);
        inputContainer.addClickableChild(removeButtonClickable);

        removeButtons.add(removeButtonClickable);

        rowY += 30;
    }


    @Override
    protected void init() {
        inputContainer = new ScrollableContainer(0, 30, this.width, this.height - 60, Text.of("Notifications on Regex Input"), 0.5F);
        this.addDrawableChild(inputContainer);

        rowY = 40;
        notifications.clear();
        removeButtons.clear();
        inputContainer.clearChildren();

        for (Pair<Pattern, String> notification : ChatNotifications.config.getValue()) {
            addRow(notification);
        }

        this.addDrawableChild(ButtonWidgetUtils.build(Text.of("Save and Exit"), b -> {
            saveConfig();
            MinecraftClient.getInstance().setScreen(this.parent);
        }, this.width / 2 + 10, this.height - 25, 200, 20));

        this.addDrawableChild(ButtonWidgetUtils.build(Text.of("Add Notification"), b -> addRow(null), this.width / 2 - 210, this.height - 25, 200, 20));
    }

    @Override
    public void close() {
        saveConfig();
        PixUtils.LOGGER.info("chat notifications saved");
        parent.close();
    }
}
