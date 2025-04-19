package pixlze.guildapi.commands.test;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import pixlze.guildapi.core.commands.ClientCommand;
import pixlze.guildapi.core.components.Managers;

public class RefreshTokenResetTestCommand extends ClientCommand {
    public RefreshTokenResetTestCommand() {
        super("resetToken");
    }

    @Override
    protected LiteralArgumentBuilder<FabricClientCommandSource> getCommand(LiteralArgumentBuilder<FabricClientCommandSource> base) {
        return base.executes((context) -> {
            Managers.Net.guild.resetRefreshToken();
            return Command.SINGLE_SUCCESS;
        });
    }
}
