package pixlze.guildapi.core.handlers;

import pixlze.guildapi.core.handlers.chat.ChatHandler;
import pixlze.guildapi.core.handlers.connection.ConnectionHandler;
import pixlze.guildapi.core.handlers.discord.DiscordMessageHandler;

public final class Handlers {
    public static final ConnectionHandler Connection = new ConnectionHandler();
    public static final ChatHandler Chat = new ChatHandler();
    public static final DiscordMessageHandler Discord = new DiscordMessageHandler();

    public static void init() {
        Connection.init();
        Chat.init();
        Discord.init();
    }
}
