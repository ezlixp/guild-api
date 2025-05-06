package pixlze.guildapi.core.components;

import pixlze.guildapi.core.handlers.chat.ChatHandler;
import pixlze.guildapi.core.handlers.connection.ConnectionHandler;
import pixlze.guildapi.core.handlers.discord.SocketEventHandler;

public final class Handlers {
    public static final ConnectionHandler Connection = new ConnectionHandler();
    public static final ChatHandler Chat = new ChatHandler();
    public static final SocketEventHandler Discord = new SocketEventHandler();

    public static void init() {
        Connection.init();
        Chat.init();
        Discord.init();
    }
}
