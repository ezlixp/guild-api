package pixlze.guildapi.commands.test;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.text.Text;
import pixlze.guildapi.core.commands.ClientCommand;
import pixlze.guildapi.mc.event.WynnChatMessage;
import pixlze.guildapi.utils.McUtils;

public class TomeRewardTestCommand extends ClientCommand {
    public TomeRewardTestCommand() {
        super("TomeRewardTest",
                ClientCommandManager.literal("tome").then(ClientCommandManager.argument("username", StringArgumentType.word()).executes((context) -> {
                    String username = StringArgumentType.getString(context, "username");
                    Text tomeGivenMessage = Text.literal("§b\uDAFF\uDFFC\uE006\uDAFF\uDFFF\uE002\uDAFF\uDFFE§b §etest rewarded §ea Guild Tome§b to §e" + username);
                    WynnChatMessage.EVENT.invoker().interact(tomeGivenMessage);
                    McUtils.sendLocalMessage(tomeGivenMessage, Text.empty(), false);
                    return Command.SINGLE_SUCCESS;
                }))
        );

    }
}
