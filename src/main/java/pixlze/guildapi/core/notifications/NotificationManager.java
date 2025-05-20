package pixlze.guildapi.core.notifications;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.components.Manager;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.screens.notifications.widgets.NotificationsEditListWidget;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationManager extends Manager {
    private static final File NOTIFICATIONS_DIR = GuildApi.getModStorageDir("notifications");
    private final Map<Class<? extends Trigger>, List<Notification<? extends Trigger>>> notifications = new HashMap<>();

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
    public <T extends Trigger> List<Notification<T>> getNotifications(Class<T> clazz) {
        List<?> temp = notifications.getOrDefault(clazz, new ArrayList<>());
        return (List<Notification<T>>) temp;
    }

    public void saveNotifications(List<NotificationsEditListWidget.Entry> entries) {
        List<Notification<Trigger.CHAT>> notifs = getNotifications(Trigger.CHAT.class);
        notifs.clear();
        for (NotificationsEditListWidget.Entry entry : entries) {
            Notification<Trigger.CHAT> notif = entry.getNotification();
            if (notif != null)
                notifs.add(notif);
        }
        saveNotifications();
    }

    private void saveNotifications() {
        JsonArray allNotifications = new JsonArray();
        for (Notification<Trigger.CHAT> chat : getNotifications(Trigger.CHAT.class)) {
            JsonObject obj = new JsonObject();
            obj.addProperty("triggerBy", "CHAT");
            obj.addProperty("trigger", chat.trigger.toString());
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
                        Notification<Trigger.CHAT> notification = Notification.ofChat(trigger, display);
                        if (notifications.containsKey(Trigger.CHAT.class))
                            notifications.get(Trigger.CHAT.class).add(notification);
                        else {
                            List<Notification<Trigger.CHAT>> temp = new ArrayList<>();
                            temp.add(notification);
                            notifications.put(Trigger.CHAT.class, (List<Notification<? extends Trigger>>) (List<?>) temp);
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
