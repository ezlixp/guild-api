package pixlze.guildapi.commands.guildresources.tomelist;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.Managers;
import pixlze.guildapi.core.commands.ClientCommand;
import pixlze.guildapi.utils.JsonUtils;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.NetUtils;
import pixlze.guildapi.utils.type.Prepend;

class AddSubCommand extends ClientCommand {
    private static final String ENDPOINT = "guilds/tomes/";

    public AddSubCommand() {
        super("add");
    }

    @Override
    protected LiteralArgumentBuilder<FabricClientCommandSource> getCommand(LiteralArgumentBuilder<FabricClientCommandSource> base) {
        return base.executes((context) -> {
            Managers.Net.guild.post(ENDPOINT + Managers.Net.guild.guildId, JsonUtils.toJsonObject("{\"username\":\"" + McUtils.playerName() + "\"}")).whenCompleteAsync((res, exception) -> {
                try {
                    NetUtils.applyDefaultCallback(res, exception, (response) -> McUtils.sendLocalMessage(Text.literal("§aSuccessfully added to the tome queue"), Prepend.DEFAULT.get(), false),
                            (error) -> {
                                if (error.equals("User already in tome list.")) {
                                    McUtils.sendLocalMessage(Text.literal("§eYou are already in the tome list. Wait until you receive a tome to re-add yourself."), Prepend.DEFAULT.get(), false);
                                } else {
                                    McUtils.sendLocalMessage(Text.literal("§cCould not add to tome list. Reason: " + error), Prepend.DEFAULT.get(), false);
                                }
                            });
                } catch (Exception e) {
                    McUtils.sendLocalMessage(Text.literal("§cSomething went wrong"), Prepend.DEFAULT.get(), false);
                    GuildApi.LOGGER.error("tomelist add error: {} {}", e, e.getMessage());
                }
            });
            return Command.SINGLE_SUCCESS;
        });
    }
}
