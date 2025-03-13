package pixlze.guildapi.commands.discord;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.socket.client.Ack;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.json.JSONArray;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.Managers;
import pixlze.guildapi.core.commands.ClientCommand;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.type.Prepend;

public class DiscordOnlineCommand extends ClientCommand {
    public DiscordOnlineCommand() {
        super("online");
    }

    protected LiteralArgumentBuilder<FabricClientCommandSource> getCommand(LiteralArgumentBuilder<FabricClientCommandSource> base) {
        return base.executes((context) -> {
            if (Managers.Net.socket == null || Managers.Net.socket.discordSocket == null) {
                McUtils.sendLocalMessage(Text.literal("Still connecting to chat server...")
                        .setStyle(Style.EMPTY.withColor(Formatting.YELLOW)), Prepend.DEFAULT.get(), false);
                return 0;
            }
            Managers.Net.socket.emit(Managers.Net.socket.discordSocket, "listOnline", (Ack) args -> {
                if (args[0] instanceof JSONArray data) {
                    try {
                        MutableText message = Text.literal("Online mod users: ");
                        for (int i = 0; i < data.length(); i++) {
                            message.append(data.getString(i));
                            if (i != data.length() - 1) message.append(", ");
                        }
                        message.setStyle(Style.EMPTY.withColor(Formatting.GREEN));
                        McUtils.sendLocalMessage(message, Prepend.GUILD.getWithStyle(Style.EMPTY.withColor(Formatting.GREEN)), true);
                    } catch (Exception e) {
                        GuildApi.LOGGER.error("error parsing online users: {} {}", e, e.getMessage());
                    }
                }
            });
            return Command.SINGLE_SUCCESS;
        });
    }
}
