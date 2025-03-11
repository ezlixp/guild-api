package pixlze.guildapi.commands.base;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.commands.ClientCommand;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.type.Prepend;

import java.util.ArrayList;
import java.util.List;

import static pixlze.guildapi.GuildApi.MOD_VERSION;

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
        ArrayList<ClientCommand> out = new ArrayList<>(List.of(new ClientCommandHelpCommand()));
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
            McUtils.sendLocalMessage(Text.of("§a§lGuild API §r§av" + MOD_VERSION + " by §lpixlze§r§a.\n§fType /guildapi help for a list of commands."), Prepend.DEFAULT.get(), false);
            return Command.SINGLE_SUCCESS;
        });
        return base;
    }
}
