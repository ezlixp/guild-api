package pixlze.guildapi.commands.base;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import pixlze.guildapi.core.commands.ClientCommand;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.type.Prepend;

import static pixlze.guildapi.GuildApi.BASE_INFO;

public class InfoCommand extends ClientCommand {
    public InfoCommand() {
        super("info");
    }

    @Override
    protected LiteralArgumentBuilder<FabricClientCommandSource> getCommand(LiteralArgumentBuilder<FabricClientCommandSource> base) {
        return base.executes((context) -> {
            McUtils.sendLocalMessage(BASE_INFO, Prepend.DEFAULT.get(), false);
            return Command.SINGLE_SUCCESS;
        });
    }
}
