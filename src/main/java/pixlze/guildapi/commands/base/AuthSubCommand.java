package pixlze.guildapi.commands.base;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import pixlze.guildapi.core.commands.ClientCommand;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.type.Prepend;

public class AuthSubCommand extends ClientCommand {
    public AuthSubCommand() {
        super("login");
    }

    @Override
    protected LiteralArgumentBuilder<FabricClientCommandSource> getCommand(LiteralArgumentBuilder<FabricClientCommandSource> base) {
        return base.executes((context) -> {
            if (Managers.Net.guild.isDisabled()) {
                Managers.Net.guild.login();
                return Command.SINGLE_SUCCESS;
            }
            McUtils.sendLocalMessage(Text.literal("§aYou are already logged in!"), Prepend.DEFAULT.get(), false);
            return 0;
        });
    }
}
