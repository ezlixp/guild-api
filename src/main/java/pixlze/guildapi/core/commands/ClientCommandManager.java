package pixlze.guildapi.core.commands;

import pixlze.guildapi.commands.ClientCommandHelpCommand;
import pixlze.guildapi.commands.discord.DiscordBlockClientCommand;
import pixlze.guildapi.commands.guildresources.RaidRewardsListClientCommand;
import pixlze.guildapi.commands.guildresources.TomeListClientCommand;
import pixlze.guildapi.core.Manager;

public class ClientCommandManager extends Manager {
    public void init() {
        registerCommand(new RaidRewardsListClientCommand());
        registerCommand(new TomeListClientCommand());
        registerCommand(new DiscordBlockClientCommand());

        registerCommand(new ClientCommandHelpCommand());
    }

    private void registerCommand(ClientCommand command) {
        command.register();
    }
}
