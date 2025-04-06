package pixlze.guildapi.commands.base;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import pixlze.guildapi.core.commands.ClientCommand;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.type.Prepend;

import static pixlze.guildapi.GuildApi.MOD_VERSION;

public class InfoCommand extends ClientCommand {
    public InfoCommand() {
        super("info");
    }

    @Override
    protected LiteralArgumentBuilder<FabricClientCommandSource> getCommand(LiteralArgumentBuilder<FabricClientCommandSource> base) {
        return base.executes((context) -> {
            McUtils.sendLocalMessage(Text.of("§a§lGuild API §r§av" + MOD_VERSION + " by §lpixlze§r§a.\n§fType /guildapi help for a list of commands."), Prepend.DEFAULT.get(), false);
            return Command.SINGLE_SUCCESS;
        });
    }
}
