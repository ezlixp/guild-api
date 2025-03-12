package pixlze.guildapi.core.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.type.Prepend;

import java.util.List;
import java.util.stream.Stream;

public abstract class ClientCommand {
    private final String literal;

    public ClientCommand(String literal) {
        this.literal = literal;
    }

    protected String getLiteral() {
        return literal;
    }

    protected List<String> getAliases() {
        return List.of();
    }

    protected List<ClientCommand> getSubCommands() {
        return List.of();
    }

    protected abstract LiteralArgumentBuilder<FabricClientCommandSource> getCommand(LiteralArgumentBuilder<FabricClientCommandSource> base);

    public List<LiteralArgumentBuilder<FabricClientCommandSource>> getCommands() {
        return Stream.concat(Stream.of(getCommand(ClientCommandManager.literal(getLiteral()))), getAliases().stream().map(ClientCommandManager::literal).map(this::getCommand)).toList();
    }

    public void register() {
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> {
            for (LiteralArgumentBuilder<FabricClientCommandSource> command : getCommands()) {
                dispatcher.register(command);
            }
        }));
    }

    protected void syntaxError() {
        McUtils.sendLocalMessage(Text.literal("§cInvalid arguments, please check the syntax and try again."), Prepend.DEFAULT.get(), false);
    }
}
