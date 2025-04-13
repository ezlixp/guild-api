package pixlze.guildapi.commands.test;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import pixlze.guildapi.core.commands.ClientCommand;
import pixlze.guildapi.core.components.Managers;

public class DiscordMessageTestCommand extends ClientCommand {
    public DiscordMessageTestCommand() {
        super("testmessage");
    }

    @Override
    protected LiteralArgumentBuilder<FabricClientCommandSource> getCommand(LiteralArgumentBuilder<FabricClientCommandSource> base) {
        return base.then(ClientCommandManager.argument("message", StringArgumentType.greedyString())
                .executes((context) -> {
                    Managers.DiscordSocket.emit("wynnMessage", StringArgumentType.getString(context, "message")
                            .replaceAll("&", "§"));
                    return Command.SINGLE_SUCCESS;
                }));

    }
}
