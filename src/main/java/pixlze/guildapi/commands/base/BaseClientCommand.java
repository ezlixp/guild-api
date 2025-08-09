package pixlze.guildapi.commands.base;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.commands.base.screen.OpenConfigSubComand;
import pixlze.guildapi.commands.base.screen.OpenMenuSubComand;
import pixlze.guildapi.commands.base.screen.OpenNotificationsSubComand;
import pixlze.guildapi.core.commands.ClientCommand;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.screens.menu.MenuScreen;
import pixlze.guildapi.utils.McUtils;

import java.util.ArrayList;
import java.util.List;

public class BaseClientCommand extends ClientCommand {
    public BaseClientCommand() {
        super("guildapi");
    }

    @Override
    protected List<String> getAliases() {
        return List.of("gapi");
    }

    @Override
    protected List<ClientCommand> getSubCommands() {
        ArrayList<ClientCommand> out = new ArrayList<>(List.of(
                new ClientCommandHelpCommand(), new InfoCommand(), new AuthSubCommand(),
                new OpenMenuSubComand(), new OpenConfigSubComand(), new OpenNotificationsSubComand()
        ));
        if (GuildApi.isDevelopment() || GuildApi.isTesting()) {
            out.add(new TestCommandHelpCommand());
        }
        return out;
    }

    @Override
    protected LiteralArgumentBuilder<FabricClientCommandSource> getCommand(LiteralArgumentBuilder<FabricClientCommandSource> base) {
        for (ClientCommand sub : getSubCommands()) {
            for (LiteralArgumentBuilder<FabricClientCommandSource> command : sub.getCommands()) {
                base = base.then(command);
            }
        }
        base.executes((context) -> {
            Managers.Tick.scheduleNextTick(() -> McUtils.mc().setScreen(new MenuScreen(McUtils.mc().currentScreen)));
            return Command.SINGLE_SUCCESS;
        });
        return base;
    }
}
