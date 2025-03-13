package pixlze.guildapi.core.commands;

import pixlze.guildapi.commands.base.BaseClientCommand;
import pixlze.guildapi.commands.discord.DiscordBridgeCommand;
import pixlze.guildapi.commands.discord.DiscordOnlineCommand;
import pixlze.guildapi.commands.discord.discordblocklist.DiscordBlockClientCommand;
import pixlze.guildapi.commands.guildresources.raidrewardslist.RaidRewardsListClientCommand;
import pixlze.guildapi.commands.guildresources.tomelist.TomeListClientCommand;
import pixlze.guildapi.core.Manager;

public class ClientCommandManager extends Manager {
    public void init() {
        registerCommand(new RaidRewardsListClientCommand());
        registerCommand(new TomeListClientCommand());
        
        registerCommand(new DiscordBridgeCommand());
        registerCommand(new DiscordOnlineCommand());
        registerCommand(new DiscordBlockClientCommand());

        registerCommand(new BaseClientCommand());
    }

    private void registerCommand(ClientCommand command) {
        command.register();
    }
}
