package pixlze.guildapi.commands.test;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import pixlze.guildapi.core.commands.ClientCommand;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.type.Prepend;

public class SendLocalMessageTestCommand extends ClientCommand {
    public SendLocalMessageTestCommand() {
        super("send");
    }

    @Override
    protected LiteralArgumentBuilder<FabricClientCommandSource> getCommand(LiteralArgumentBuilder<FabricClientCommandSource> base) {
        return base.then(ClientCommandManager.literal("wynn").then(ClientCommandManager.argument("message", StringArgumentType.greedyString()).executes(context -> {
            McUtils.sendLocalMessage(Text.literal(context.getArgument("message", String.class).replaceAll("&", "§")), Prepend.GUILD.get(), true);
            return Command.SINGLE_SUCCESS;
        })));
    }
}
