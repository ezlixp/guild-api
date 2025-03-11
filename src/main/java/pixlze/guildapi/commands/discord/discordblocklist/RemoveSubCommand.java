package pixlze.guildapi.commands.discord.discordblocklist;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import pixlze.guildapi.core.Managers;
import pixlze.guildapi.core.commands.ClientCommand;
import pixlze.guildapi.models.Models;
import pixlze.guildapi.utils.ColourUtils;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.NetUtils;
import pixlze.guildapi.utils.type.Prepend;

import java.util.List;

public class RemoveSubCommand extends ClientCommand {
    private static final String ENDPOINT = "user/blocked/";

    public RemoveSubCommand() {
        super("remove");
    }

    @Override
    protected List<String> getAliases() {
        return List.of("rm");
    }

    @Override
    protected LiteralArgumentBuilder<FabricClientCommandSource> getCommand(LiteralArgumentBuilder<FabricClientCommandSource> base) {
        return base.then(ClientCommandManager.argument("toRemove", StringArgumentType.word()).executes((context -> {
            String toRemove = StringArgumentType.getString(context, "toRemove");
            Managers.Net.guild.delete(ENDPOINT + McUtils.playerUUID() + "/" + toRemove).whenCompleteAsync((res, exception) -> {
                try {
                    NetUtils.applyDefaultCallback(res, exception, (resOK) -> {
                        McUtils.sendLocalMessage(Text.literal("§aSuccessfully unblocked " + toRemove + ".\n" +
                                "Messages from them will be shown again."), Prepend.GUILD.getWithStyle(ColourUtils.GREEN), true);
                        Models.DiscordMessage.unblock(toRemove);
                    }, (error) -> {
                        if (error.equals("Blocked user not found.")) {
                            McUtils.sendLocalMessage(Text.literal("§e" + toRemove + " was not blocked."), Prepend.GUILD.getWithStyle(ColourUtils.YELLOW), true);
                        }
                    });
                } catch (Exception e) {
                    NetUtils.defaultException("remove blocked", e);
                }
            });
            return Command.SINGLE_SUCCESS;
        })));
    }
}
