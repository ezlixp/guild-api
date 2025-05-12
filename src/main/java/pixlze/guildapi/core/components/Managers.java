package pixlze.guildapi.core.components;


import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.commands.ClientCommandManager;
import pixlze.guildapi.core.commands.test.TestClientCommandManager;
import pixlze.guildapi.core.config.ConfigManager;
import pixlze.guildapi.core.features.FeatureManager;
import pixlze.guildapi.core.json.JsonManager;
import pixlze.guildapi.core.mod.TickSchedulerManager;
import pixlze.guildapi.core.notifications.NotificationManager;
import pixlze.guildapi.discord.DiscordMessageManager;
import pixlze.guildapi.discord.DiscordSocketManager;
import pixlze.guildapi.net.NetManager;

public final class Managers {
    public static final JsonManager Json = new JsonManager();

    public static final NetManager Net = new NetManager();
    public static final DiscordSocketManager DiscordSocket = new DiscordSocketManager();
    public static final DiscordMessageManager Discord = new DiscordMessageManager();

    public static final FeatureManager Feature = new FeatureManager();
    public static final ConfigManager Config = new ConfigManager();
    public static final NotificationManager Notification = new NotificationManager();

    public static final ClientCommandManager Command = new ClientCommandManager();
    public static final TestClientCommandManager TestCommand = new TestClientCommandManager();

    public static final TickSchedulerManager Tick = new TickSchedulerManager();

    public static void init() {
        Json.init();
        Net.init();
        DiscordSocket.init();
        Feature.init();
        Config.init();
        Notification.init();
        Command.init();
        Tick.init();
        if (GuildApi.isDevelopment() || GuildApi.isTesting())
            TestCommand.init();
    }
}
