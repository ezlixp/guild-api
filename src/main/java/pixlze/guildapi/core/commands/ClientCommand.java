package pixlze.guildapi.core.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

// one command per object
public abstract class ClientCommand {
    private String literal; // maybe theres a way to automatically get the /___ with arguments from mc functions
    private LiteralArgumentBuilder<FabricClientCommandSource> command;
    private String description;

    public abstract void init();

    public void setCommand(LiteralArgumentBuilder<FabricClientCommandSource> command) {
        this.command = command;
    }

    public void register() {
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> dispatcher.register(command)));
    }

    public String getLiteral() {
        return literal;
    }

    public String getDescription() {
        return description;
    }

}
