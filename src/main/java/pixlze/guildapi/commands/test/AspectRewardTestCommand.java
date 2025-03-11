package pixlze.guildapi.commands.test;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import pixlze.guildapi.core.commands.ClientCommand;
import pixlze.guildapi.mc.event.WynnChatMessage;
import pixlze.guildapi.utils.McUtils;

public class AspectRewardTestCommand extends ClientCommand {
    public AspectRewardTestCommand() {
        super("aspect");
    }

    @Override
    protected LiteralArgumentBuilder<FabricClientCommandSource> getCommand(LiteralArgumentBuilder<FabricClientCommandSource> base) {
        return base.then(ClientCommandManager.argument("username", StringArgumentType.word()).executes((context) -> {
            String username = StringArgumentType.getString(context, "username");
            Text aspectGivenMessage = Text.literal("§b\uDAFF\uDFFC\uE006\uDAFF\uDFFF\uE002\uDAFF\uDFFE§b §etest§b rewarded §ean Aspect§b to §e" + username);
            WynnChatMessage.EVENT.invoker().interact(aspectGivenMessage);
            McUtils.sendLocalMessage(aspectGivenMessage, Text.empty(), false);
            return Command.SINGLE_SUCCESS;
        }));
    }
}
