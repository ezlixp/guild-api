package pixlze.guildapi.commands.discord;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import pixlze.guildapi.core.commands.ClientCommand;

public class DiscordBridgeCommand extends ClientCommand {
    public DiscordBridgeCommand() {
        super("DiscordBridge");
    }

    @Override
    protected LiteralArgumentBuilder<FabricClientCommandSource> getCommand(LiteralArgumentBuilder<FabricClientCommandSource> base) {
        return base;
    }
}
