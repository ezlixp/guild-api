package pixlze.guildapi.core.commands;

import pixlze.guildapi.commands.base.BaseClientCommand;
import pixlze.guildapi.commands.discord.discordblocklist.DiscordBlockClientCommand;
import pixlze.guildapi.commands.guildresources.raidrewardslist.RaidRewardsListClientCommand;
import pixlze.guildapi.commands.guildresources.tomelist.TomeListClientCommand;
import pixlze.guildapi.core.Manager;

public class ClientCommandManager extends Manager {
    public void init() {
        registerCommand(new RaidRewardsListClientCommand());
        registerCommand(new TomeListClientCommand());
        registerCommand(new DiscordBlockClientCommand());

        registerCommand(new BaseClientCommand());
    }

    private void registerCommand(ClientCommand command) {
        command.register();
    }
}
