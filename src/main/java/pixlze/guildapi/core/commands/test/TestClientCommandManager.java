package pixlze.guildapi.core.commands.test;

import pixlze.guildapi.commands.test.*;
import pixlze.guildapi.core.commands.ClientCommand;
import pixlze.guildapi.core.components.Manager;

import java.util.List;

public class TestClientCommandManager extends Manager {
    public TestClientCommandManager() {
        super(List.of());
    }

    // hashmap of all commands, do init and registe rseperately
    public void init() {
        registerCommand(new AspectRewardTestCommand());
        registerCommand(new DiscordMessageTestCommand());
        registerCommand(new GuildRaidTestCommand());
        registerCommand(new SetPlayerTestCommand());
        registerCommand(new TomeRewardTestCommand());
    }

    private void registerCommand(ClientCommand command) {
        command.register();
    }
}
