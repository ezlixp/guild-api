package pixlze.guildapi.commands.guildresources.raidrewardslist;

import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.Managers;
import pixlze.guildapi.core.commands.ClientCommand;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.NetUtils;
import pixlze.guildapi.utils.type.Prepend;

class SearchSubCommand extends ClientCommand {
    private static final String ENDPOINT = "guilds/raids/rewards/";

    public SearchSubCommand() {
        super("search");
    }

    @Override
    protected LiteralArgumentBuilder<FabricClientCommandSource> getCommand(LiteralArgumentBuilder<FabricClientCommandSource> base) {
        return base.executes(
                        (context) -> {
                            search(McUtils.playerName());
                            return Command.SINGLE_SUCCESS;
                        })
                .then(ClientCommandManager.argument("username", StringArgumentType.word())
                        .executes((context) -> {
                            search(StringArgumentType.getString(context, "username"));
                            return Command.SINGLE_SUCCESS;
                        })
                );
    }

    private void search(String username) {
        Managers.Net.guild.get(ENDPOINT + Managers.Net.guild.guildId + "/" + username).whenCompleteAsync((res, exception) -> {
            try {
                NetUtils.applyDefaultCallback(res, exception, (response) -> {
                    JsonObject resObject = response.getAsJsonObject();
                    McUtils.sendLocalMessage(Text.literal(resObject.get("username").getAsString())
                                    .append(": ")
                                    .append(resObject.getAsJsonObject().get("raids").getAsString())
                                    .append(" raids | ")
                                    .append(resObject.getAsJsonObject().get("aspects").getAsString())
                                    .append(" aspects | ")
                                    .append(String.format("%.2f", resObject.getAsJsonObject().get("liquidEmeralds").getAsDouble()))
                                    .append(" ¼²")
                            , Prepend.DEFAULT.get(), false);
                }, (error) -> {
                    if (error.equals("Specified user could not be found in raid rewards list."))
                        McUtils.sendLocalMessage(Text.literal("§eCould not find \"" + username + "\" in the raid rewards list."), Prepend.DEFAULT.get(), false);
                    else
                        McUtils.sendLocalMessage(Text.literal("§cCould not fetch raid rewards list. Reason: " + error), Prepend.DEFAULT.get(), false);
                });
            } catch (Exception e) {
                McUtils.sendLocalMessage(Text.literal("§cSomething went wrong. Check logs for more details."), Prepend.DEFAULT.get(), false);
                GuildApi.LOGGER.error("raid rewards search error: {} {}", e, e.getMessage());
            }
        });
    }
}
