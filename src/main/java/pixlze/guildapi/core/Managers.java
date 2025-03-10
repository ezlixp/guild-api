package pixlze.guildapi.core;


import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.commands.ClientCommandManager;
import pixlze.guildapi.core.commands.test.TestClientCommandManager;
import pixlze.guildapi.core.config.ConfigManager;
import pixlze.guildapi.core.features.FeatureManager;
import pixlze.guildapi.net.NetManager;

public final class Managers {
    public static final NetManager Net = new NetManager();

    public static final FeatureManager Feature = new FeatureManager();
    public static final ConfigManager Config = new ConfigManager();

    public static final ClientCommandManager Command = new ClientCommandManager();
    public static final TestClientCommandManager TestCommand = new TestClientCommandManager();

    public static void init() {
        Net.init();
        Feature.init();
        Config.init();
        Command.init();
        if (GuildApi.isDevelopment() || GuildApi.isTesting())
            TestCommand.init();
    }
}
