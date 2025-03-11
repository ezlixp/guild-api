package pixlze.guildapi.commands.discord.discordblocklist;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.Managers;
import pixlze.guildapi.core.commands.ClientCommand;
import pixlze.guildapi.models.Models;
import pixlze.guildapi.utils.ColourUtils;
import pixlze.guildapi.utils.JsonUtils;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.NetUtils;
import pixlze.guildapi.utils.type.Prepend;

class AddSubCommand extends ClientCommand {
    private static final String ENDPOINT = "user/blocked/";

    public AddSubCommand() {
        super("add");
    }

    @Override
    protected LiteralArgumentBuilder<FabricClientCommandSource> getCommand(LiteralArgumentBuilder<FabricClientCommandSource> base) {
        return base.then(ClientCommandManager.argument("toBlock", StringArgumentType.word()).executes((context) -> {
            String toBlock = StringArgumentType.getString(context, "toBlock");
            if (Models.DiscordMessage.getBlocked().size() >= 50) {
                McUtils.sendLocalMessage(Text.literal("§cMy brother in christ why do you want to block more than 50 people."), Prepend.GUILD.getWithStyle(ColourUtils.RED), true);
                return 0;
            }
            if (toBlock.equalsIgnoreCase(McUtils.playerName())) {
                McUtils.sendLocalMessage(Text.literal("§cYou can't block yourself."), Prepend.GUILD.getWithStyle(ColourUtils.RED), true);
                return 0;
            }
            Managers.Net.guild.post(ENDPOINT + McUtils.playerUUID(), JsonUtils.toJsonObject("{toBlock:\"" + toBlock + "\"}")).whenCompleteAsync((res, exception) -> {
                try {
                    NetUtils.applyDefaultCallback(res, exception, (resOK) -> {
                        McUtils.sendLocalMessage(Text.literal("§aSuccessfully blocked \"" + toBlock + "\".\n" +
                                "All purple discord messages from them will be hidden."), Prepend.GUILD.getWithStyle(ColourUtils.GREEN), true);
                        Models.DiscordMessage.block(toBlock);
                    }, (error) -> {
                        if (error.equals("Blocked list full.")) {
                            McUtils.sendLocalMessage(Text.literal("§cMy brother in christ why do you want to block more than 50 people."), Prepend.GUILD.getWithStyle(ColourUtils.RED), true);
                        } else if (error.equals("User already in block list.")) {
                            McUtils.sendLocalMessage(Text.literal("§e" + toBlock + " has already been blocked."), Prepend.GUILD.getWithStyle(ColourUtils.YELLOW), true);
                        } else {
                            McUtils.sendLocalMessage(Text.literal("§cSomething went wrong: " + error), Prepend.DEFAULT.get(), false);
                            GuildApi.LOGGER.error("block add error: {}", error);
                        }
                    });
                } catch (Exception e) {
                    NetUtils.defaultException("add blocked", e);
                }
            });
            return Command.SINGLE_SUCCESS;
        }));
    }
}
