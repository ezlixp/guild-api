package pixlze.guildapi.core.components;

import pixlze.guildapi.core.handlers.chat.ChatHandler;
import pixlze.guildapi.core.handlers.connection.ConnectionHandler;
import pixlze.guildapi.core.handlers.discord.SocketEventHandler;
import pixlze.guildapi.core.handlers.notification.NotificationHandler;

public final class Handlers {
    public static final ConnectionHandler Connection = new ConnectionHandler();
    public static final ChatHandler Chat = new ChatHandler();
    public static final NotificationHandler Notification = new NotificationHandler();
    public static final SocketEventHandler Discord = new SocketEventHandler();

    public static void init() {
        Connection.init();
        Chat.init();
        Notification.init();
        Discord.init();
    }
}
