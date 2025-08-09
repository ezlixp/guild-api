package pixlze.guildapi.commands.test;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import pixlze.guildapi.core.commands.ClientCommand;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.utils.McUtils;

public class SetPlayerTestCommand extends ClientCommand {
    public SetPlayerTestCommand() {
        super("setplayer");
    }

    @Override
    protected LiteralArgumentBuilder<FabricClientCommandSource> getCommand(LiteralArgumentBuilder<FabricClientCommandSource> base) {
        return base.then(ClientCommandManager.argument("username", StringArgumentType.word()).executes(context -> {
            McUtils.devName = StringArgumentType.getString(context, "username");
            Managers.DiscordSocket.disable();
            Managers.Net.wynn.reloadWynnInfo();
            Managers.Net.guild.disable();
            Managers.Net.guild.enable();
            Managers.DiscordSocket.initSocket();
            return Command.SINGLE_SUCCESS;
        }));
    }
}
