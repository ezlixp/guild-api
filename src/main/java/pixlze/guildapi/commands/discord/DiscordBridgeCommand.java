package pixlze.guildapi.commands.discord;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import pixlze.guildapi.core.Managers;
import pixlze.guildapi.core.commands.ClientCommand;
import pixlze.guildapi.core.features.FeatureState;
import pixlze.guildapi.features.discord.DiscordBridgeFeature;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.type.Prepend;

import java.util.List;
import java.util.Map;

public class DiscordBridgeCommand extends ClientCommand {
    public DiscordBridgeCommand() {
        super("discord");
    }

    @Override
    public List<String> getAliases() {
        return List.of("dc");
    }

    @Override
    protected LiteralArgumentBuilder<FabricClientCommandSource> getCommand(LiteralArgumentBuilder<FabricClientCommandSource> base) {
        return base.then(ClientCommandManager.argument("message", StringArgumentType.greedyString())
                .executes((context) -> {
                    String message = StringArgumentType.getString(context, "message");
                    message = message.replaceAll("[\u200C\uE087\uE013\u2064\uE071\uE012\uE000\uE089\uE088\uE07F\uE08B\uE07E\uE080ÁÀ֎]", "");
                    if (message.isBlank()) return 0;
                    if (Managers.Net.socket == null || Managers.Net.socket.discordSocket == null) {
                        McUtils.sendLocalMessage(Text.literal("Still connecting to chat server...")
                                .setStyle(Style.EMPTY.withColor(Formatting.YELLOW)), Prepend.DEFAULT.get(), false);
                        return 0;
                    }
                    if (!Managers.Net.socket.discordSocket.connected()) {
                        McUtils.sendLocalMessage(Text.literal("Chat server not connected. Type /reconnect to try to connect.")
                                .setStyle(Style.EMPTY.withColor(Formatting.RED)), Prepend.DEFAULT.get(), false);
                        return 0;

                    }
                    if (Managers.Feature.getFeatureState(Managers.Feature.getFeatureInstance(DiscordBridgeFeature.class)) != FeatureState.ENABLED) {
                        McUtils.sendLocalMessage(Text.literal("Discord bridging not enabled. Please enable in config and try again.")
                                .setStyle(Style.EMPTY.withColor(Formatting.RED)), Prepend.DEFAULT.get(), false);
                        return 0;
                    }
                    Managers.Net.socket.emit(Managers.Net.socket.discordSocket, "discordOnlyWynnMessage", McUtils.playerName() + ": " + message);
                    Managers.Net.socket.emit(Managers.Net.socket.discordSocket, "discordMessage", Map.of("Author", McUtils.playerName(), "Content", message, "WynnGuildId", Managers.Net.guild.guildId));
                    return Command.SINGLE_SUCCESS;
                })).executes(context -> {
            syntaxError();
            return Command.SINGLE_SUCCESS;
        });
    }
}
