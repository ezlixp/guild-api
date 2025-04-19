package pixlze.guildapi.commands.guildresources.tomelist;

import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import pixlze.guildapi.core.commands.ClientCommand;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.utils.ExceptionUtils;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.NetUtils;
import pixlze.guildapi.utils.type.Prepend;

class SearchSubCommand extends ClientCommand {
    private static final String ENDPOINT = "guilds/tomes/";

    public SearchSubCommand() {
        super("search");
    }

    @Override
    protected LiteralArgumentBuilder<FabricClientCommandSource> getCommand(LiteralArgumentBuilder<FabricClientCommandSource> base) {
        return base.executes((context) -> {
            search(McUtils.playerName());
            return Command.SINGLE_SUCCESS;
        }).then(ClientCommandManager.argument("username", StringArgumentType.word()).executes((context) -> {
            search(StringArgumentType.getString(context, "username"));
            return Command.SINGLE_SUCCESS;
        }));
    }

    private void search(String username) {
        Managers.Net.guild.get(ENDPOINT + Managers.Net.guild.guildId + "/" + username, false).whenCompleteAsync((res, exception) -> {
            try {
                NetUtils.applyDefaultCallback(res, exception, (response) -> {
                    JsonObject resBody = response.getAsJsonObject();
                    McUtils.sendLocalMessage(Text.literal(resBody.get("username").getAsString() + " is at position " + resBody.get("position").getAsString() + " in the tome queue.").withColor(0xFFFFFF), Prepend.DEFAULT.get(), false);
                }, (error) -> {
                    if (error.equals("Specified user could not be found in tome list."))
                        McUtils.sendLocalMessage(Text.literal("§eCould not find \"" + username + "\" in the tome queue."), Prepend.DEFAULT.get(), false);
                    else
                        McUtils.sendLocalMessage(Text.literal("§cCould not fetch tome list. Reason: " + error), Prepend.DEFAULT.get(), false);
                });
            } catch (Exception e) {
                ExceptionUtils.defaultException("tomelist search", e);
            }
        });
    }
}
