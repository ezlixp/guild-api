package pixlze.guildapi.core.notifications;

import pixlze.guildapi.core.components.Manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationManager extends Manager {
    private final Map<? extends NotificationTriggerPacket, List<Notification<? extends NotificationTriggerPacket>>> notifications = new HashMap<>();

    public NotificationManager() {
        super(List.of());
    }

    @Override
    public void init() {

    }
}
