package pixlze.guildapi.components;

import pixlze.guildapi.handlers.chat.ChatHandler;
import pixlze.guildapi.handlers.discord.DiscordMessageHandler;

public final class Handlers {
    public static final ChatHandler Chat = new ChatHandler();
    public static final DiscordMessageHandler Discord = new DiscordMessageHandler();

    public static void init() {
        Chat.init();
        Discord.init();
    }
}
