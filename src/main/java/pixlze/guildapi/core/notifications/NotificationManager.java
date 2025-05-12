package pixlze.guildapi.core.notifications;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.components.Manager;
import pixlze.guildapi.core.components.Managers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationManager extends Manager {
    private static final File NOTIFICATIONS_DIR = GuildApi.getModStorageDir("notifications");
    private final Map<Class<? extends NotificationTrigger>, List<Notification<? extends NotificationTrigger>>> notifications = new HashMap<>();

    private final File notificationsFile;
    private JsonArray notificationsArray;

    public NotificationManager() {
        super(List.of());
        notificationsFile = new File(NOTIFICATIONS_DIR, "notifications.json");
    }

    @Override
    public void init() {
        try {
            notificationsArray = Managers.Json.loadJsonFromFile(notificationsFile).getAsJsonArray();
        } catch (Exception e) {
            GuildApi.LOGGER.warn("notifications load error: {} {}", e, e.getMessage());
            notificationsArray = new JsonArray();
        }
        registerNotifications();
        saveNotifications();
    }

    @SuppressWarnings("unchecked")
    public <T extends NotificationTrigger> List<Notification<T>> getNotifications(Class<T> clazz) {
        List<?> temp = notifications.getOrDefault(clazz, new ArrayList<>());
        return (List<Notification<T>>) temp;
    }

    private void saveNotifications() {
        JsonArray allNotifications = new JsonArray();
        for (Notification<NotificationTrigger.CHAT> chat : getNotifications(NotificationTrigger.CHAT.class)) {
            JsonObject obj = new JsonObject();
            obj.addProperty("triggerBy", "CHAT");
            obj.add("trigger", chat.trigger);
            obj.addProperty("display", chat.displayText);
            allNotifications.add(obj);
        }
        Managers.Json.saveJsonAsFile(notificationsFile, allNotifications);
    }

    @SuppressWarnings("unchecked")
    private void registerNotifications() {
        notifications.clear();
        try {
            notificationsArray.forEach((element) -> {
                JsonObject asObject = element.getAsJsonObject();
                try {
                    String type = asObject.get("triggerBy").getAsString();
                    String trigger = asObject.get("trigger").getAsString();
                    String display = asObject.get("display").getAsString();
                    if (type.equals("CHAT")) {
                        Notification<NotificationTrigger.CHAT> notification = Notification.ofChat(trigger, display);
                        if (notifications.containsKey(NotificationTrigger.CHAT.class))
                            notifications.get(NotificationTrigger.CHAT.class).add(notification);
                        else {
                            List<Notification<NotificationTrigger.CHAT>> temp = new ArrayList<>();
                            temp.add(notification);
                            notifications.put(NotificationTrigger.CHAT.class, (List<Notification<? extends NotificationTrigger>>) (List<?>) temp);
                        }
                    } else
                        GuildApi.LOGGER.warn("illegal notification trigger type: {}", type);
                } catch (Exception e) {
                    GuildApi.LOGGER.warn("notifications parse error: {} {}", e, e.getMessage());
                }
            });
        } catch (Exception e) {
            GuildApi.LOGGER.warn("notifications register error: {} {}", e, e.getMessage());
        }
    }
}
