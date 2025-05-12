package pixlze.guildapi.core.handlers.notification;

import net.minecraft.text.Text;
import pixlze.guildapi.core.components.Handler;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.core.handlers.chat.event.ChatMessageReceived;
import pixlze.guildapi.core.notifications.Notification;
import pixlze.guildapi.core.notifications.NotificationTrigger;
import pixlze.guildapi.utils.text.TextUtils;
import pixlze.guildapi.utils.text.type.TextParseOptions;

public class NotificationHandler extends Handler {
    @Override
    public void init() {
        ChatMessageReceived.EVENT.register(this::onWynnMessage);
    }

    private void onWynnMessage(Text message) {
        for (Notification<NotificationTrigger.CHAT> notification : Managers.Notification.getNotifications(NotificationTrigger.CHAT.class)) {
            notification.apply(new NotificationTrigger.CHAT(TextUtils.parseStyled(message, TextParseOptions.DEFAULT)));
        }
    }
}
