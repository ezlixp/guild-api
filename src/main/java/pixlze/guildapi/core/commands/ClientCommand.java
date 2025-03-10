package pixlze.guildapi.core.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import pixlze.guildapi.GuildApi;

public abstract class ClientCommand {
    private final String name;
    private LiteralArgumentBuilder<FabricClientCommandSource> command;

    public ClientCommand(String name) {
        this.name = name;
    }

    public ClientCommand(String name, LiteralArgumentBuilder<FabricClientCommandSource> command) {
        this.name = name;
        this.command = command;
    }


    public void setCommand(LiteralArgumentBuilder<FabricClientCommandSource> command) {
        this.command = command;
    }

    public void register() {
        if (command != null)
            ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> dispatcher.register(command)));
        else {
            GuildApi.LOGGER.warn("null command at {}", name);
        }
    }
}
