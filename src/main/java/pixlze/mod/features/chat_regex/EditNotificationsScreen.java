package pixlze.mod.features.chat_regex;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import pixlze.mod.config.types.SubConfigScreen;
import pixlze.utils.gui.ButtonWidgetFactory;
import pixlze.utils.gui.ClickableChild;
import pixlze.utils.gui.ScrollableContainer;

import java.util.ArrayList;
import java.util.regex.Pattern;


public class EditNotificationsScreen extends SubConfigScreen {
    protected final ArrayList<Pair<ClickableChild<TextFieldWidget>, ClickableChild<TextFieldWidget>>> notifications;
    protected final ArrayList<ClickableChild<ButtonWidget>> removeButtons;
    protected int rowY = 60;
    private ScrollableContainer inputContainer;


    public EditNotificationsScreen() {
        super(Text.of("Notifications"));
        notifications = new ArrayList<>();
        removeButtons = new ArrayList<>();
    }

    private void saveConfig() {
        ChatNotifications.config.getValue().clear();
        for (Pair<ClickableChild<TextFieldWidget>, ClickableChild<TextFieldWidget>> notif : notifications) {
            if (!notif.getA().getChild().getText().isEmpty() && !notif.getB().getChild().getText().isEmpty()) {
                ChatNotifications.config.getValue().add(new Pair<>(Pattern.compile(notif.getA().getChild().getText()), notif.getB().getChild().getText()));
            }
        }
    }

    private void removeNotification(ButtonWidget b, ClickableChild<ButtonWidget> asChild) {
        int index = removeButtons.indexOf(asChild);
        inputContainer.queueRemove(notifications.get(index).getA());
        inputContainer.queueRemove(notifications.get(index).getB());
        inputContainer.queueRemove(removeButtons.removeLast());

        notifications.remove(index);

        for (int i = index; i < notifications.size(); ++i) {
            notifications.get(i).getA().getChild().setY(notifications.get(i).getA().getY() - 30);
            notifications.get(i).getB().getChild().setY(notifications.get(i).getB().getY() - 30);
        }
        rowY -= 30;
    }

    private void addRow(@Nullable Pair<Pattern, String> notification) {
        TextFieldWidget regexInput = new TextFieldWidget(this.textRenderer, this.width / 3 + 10, rowY, 100, 20, Text.of("Input Regex"));
        regexInput.setEditable(true);
        regexInput.setPlaceholder(Text.of("Input Regex"));
        if (notification != null) regexInput.write(notification.getA().toString());
        ClickableChild<TextFieldWidget> regexInputClickable = new ClickableChild<>(regexInput);
        inputContainer.addClickableChild(regexInputClickable);

        TextFieldWidget notificationInput = new TextFieldWidget(this.textRenderer, this.width / 3 + 120, rowY, 100, 20, Text.of("Input Notification"));
        notificationInput.setEditable(true);
        notificationInput.setPlaceholder(Text.of("Input Notification"));
        if (notification != null) notificationInput.write(notification.getB());
        ClickableChild<TextFieldWidget> notificationInputClickable = new ClickableChild<>(notificationInput);
        inputContainer.addClickableChild(notificationInputClickable);

        notifications.add(new Pair<>(regexInputClickable, notificationInputClickable));

        ButtonWidget removeButton = ButtonWidgetFactory.build(Text.of("Remove"), b -> removeNotification(b, new ClickableChild<>(b)), 2 * this.width / 3 - 60, rowY, 50, 20);
        ClickableChild<ButtonWidget> removeButtonClickable = new ClickableChild<>(removeButton);
        inputContainer.addClickableChild(removeButtonClickable);

        removeButtons.add(removeButtonClickable);

        rowY += 30;
    }


    @Override
    protected void init() {
        inputContainer = new ScrollableContainer(0, 50, this.width, this.height - 80, Text.of("Notifications on Regex Input"), 0.5F);
        this.addDrawableChild(inputContainer);

        rowY = 60;
        notifications.clear();
        removeButtons.clear();
        inputContainer.clearChildren();

        for (Pair<Pattern, String> notification : ChatNotifications.config.getValue()) {
            addRow(notification);
        }

        this.addDrawableChild(ButtonWidgetFactory.build(Text.of("Save and Exit"), b -> {
            saveConfig();
            MinecraftClient.getInstance().setScreen(this.parent);
        }, this.width / 2 + 10, this.height - 25, 200, 20));

        this.addDrawableChild(ButtonWidgetFactory.build(Text.of("Add Notification"), b -> addRow(null), this.width / 2 - 210, this.height - 25, 200, 20));
    }

    @Override
    public void close() {
        saveConfig();
        super.close();
    }
}
