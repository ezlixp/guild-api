package pixlze.mod.features.chat_regex;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import oshi.util.tuples.Pair;
import pixlze.mod.config.types.SubConfigScreen;
import pixlze.utils.gui.ButtonWidgetFactory;
import pixlze.utils.gui.ScrollableContainer;

import java.util.ArrayList;
import java.util.regex.Pattern;


public class EditNotificationsScreen extends SubConfigScreen {
    protected final ArrayList<Pair<TextFieldWidget, TextFieldWidget>> notifications;
    protected final ArrayList<ButtonWidget> removeButtons;
    protected int rowY = 60;
    private ScrollableContainer inputContainer;


    public EditNotificationsScreen() {
        super(Text.of("Notifications"));
        notifications = new ArrayList<>();
        removeButtons = new ArrayList<>();
    }

    private void saveConfig() {
        ChatNotifications.config.getValue().clear();
        for (Pair<TextFieldWidget, TextFieldWidget> notif : notifications) {
            if (!notif.getA().getText().isEmpty() && !notif.getB().getText().isEmpty()) {
                ChatNotifications.config.getValue().add(new Pair<>(Pattern.compile(notif.getA().getText()), notif.getB().getText()));
            }
        }
    }

    private void removeNotification(ButtonWidget b) {
        int index = removeButtons.indexOf(b);
        inputContainer.queueRemove(notifications.get(index).getA());
        inputContainer.queueRemove(notifications.get(index).getB());
        inputContainer.queueRemove(removeButtons.removeLast());

        notifications.remove(index);

        for (int i = index; i < notifications.size(); ++i) {
            notifications.get(i).getA().setY(notifications.get(i).getA().getY() - 30);
            notifications.get(i).getB().setY(notifications.get(i).getB().getY() - 30);
        }
        rowY -= 30;
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
            TextFieldWidget regexInput = new TextFieldWidget(this.textRenderer, this.width / 3 + 10, rowY, 100, 20, Text.of("Input Regex"));
            regexInput.setEditable(true);
            regexInput.setPlaceholder(Text.of("Input Regex"));
            regexInput.write(notification.getA().toString());
            inputContainer.addClickableChild(regexInput);

            TextFieldWidget notificationInput = new TextFieldWidget(this.textRenderer, this.width / 3 + 120, rowY, 100, 20, Text.of("Input Notification"));
            notificationInput.setEditable(true);
            notificationInput.setPlaceholder(Text.of("Input Notification"));
            notificationInput.write(notification.getB());
            inputContainer.addClickableChild(notificationInput);

            notifications.add(new Pair<>(regexInput, notificationInput));

            ButtonWidget removeButton = ButtonWidgetFactory.build(Text.of("Remove"), this::removeNotification, 2 * this.width / 3 + -60, rowY, 50, 20);
            inputContainer.addClickableChild(removeButton);

            removeButtons.add(removeButton);

            rowY += 30;
        }

        this.addDrawableChild(ButtonWidgetFactory.build(Text.of("Save and Exit"), b -> {
            saveConfig();
            MinecraftClient.getInstance().setScreen(this.parent);
        }, this.width / 2 + 10, this.height - 25, 200, 20));

        this.addDrawableChild(ButtonWidgetFactory.build(Text.of("Add Notification"), b -> {
            TextFieldWidget regexInput = new TextFieldWidget(this.textRenderer, this.width / 3 + 10, rowY, 100, 20, Text.of("Input Regex"));
            regexInput.setEditable(true);
            regexInput.setPlaceholder(Text.of("Input Regex"));
            inputContainer.addClickableChild(regexInput);

            TextFieldWidget notificationInput = new TextFieldWidget(this.textRenderer, this.width / 3 + 120, rowY, 100, 20, Text.of("Input Notification"));
            notificationInput.setEditable(true);
            notificationInput.setPlaceholder(Text.of("Input Notification"));
            inputContainer.addClickableChild(notificationInput);

            notifications.add(new Pair<>(regexInput, notificationInput));

            ButtonWidget removeButton = ButtonWidgetFactory.build(Text.of("Remove"), this::removeNotification, 2 * this.width / 3 + -60, rowY, 50, 20);
            inputContainer.addClickableChild(removeButton);

            removeButtons.add(removeButton);

            rowY += 30;

        }, this.width / 2 - 210, this.height - 25, 200, 20));
    }

    @Override
    public void close() {
        saveConfig();
        super.close();
    }
}
