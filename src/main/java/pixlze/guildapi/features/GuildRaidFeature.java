package pixlze.guildapi.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.config.Config;
import pixlze.guildapi.core.features.Feature;
import pixlze.guildapi.core.handlers.chat.event.ChatMessageReceived;
import pixlze.guildapi.mc.event.WynnChatMessage;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.text.TextUtils;
import pixlze.guildapi.utils.text.type.TextParseOptions;
import pixlze.guildapi.utils.type.Prepend;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuildRaidFeature extends Feature {
    private final Pattern RAID_PATTERN = Pattern.compile("^§[b8]((\uDAFF\uDFFC\uE001\uDB00\uDC06)|(\uDAFF\uDFFC\uE006\uDAFF\uDFFF\uE002\uDAFF\uDFFE))§[b8] §[e8](?<player1>.*?)" +
            "§[b8], §[e8](?<player2>.*?)§[b8], §[e8](?<player3>.*?)§[b8], and §[e8](?<player4>.*?)§[b8] finished §[38](?<raid>.*?)§[b8].*$");

    @Override
    public void init() {
        ChatMessageReceived.EVENT.register(this::onWynnMessage);
        if (GuildApi.isTesting()) {
            // TODO: move to testcommands
            ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> {
                dispatcher.register(ClientCommandManager.literal("raid").then(
                        ClientCommandManager.argument("raid", StringArgumentType.word()).suggests((context, builder) -> {
                                    builder.suggest("notg");
                                    builder.suggest("nol");
                                    builder.suggest("tcc");
                                    builder.suggest("tna");
                                    return builder.buildFuture();
                                })
                                .then(ClientCommandManager.argument("player1", StringArgumentType.word())
                                        .then(ClientCommandManager.argument("player2", StringArgumentType.word())
                                                .then(ClientCommandManager.argument("player3", StringArgumentType.word())
                                                        .then(ClientCommandManager.argument("player4", StringArgumentType.word())
                                                                .executes((context) -> {
                                                                    String raid = StringArgumentType.getString(context, "raid");
                                                                    String player1 = StringArgumentType.getString(context, "player1");
                                                                    String player2 = StringArgumentType.getString(context, "player2");
                                                                    String player3 = StringArgumentType.getString(context, "player3");
                                                                    String player4 = StringArgumentType.getString(context, "player4");
                                                                    Text raidFinishedMessage = Text.literal("§b\uDAFF\uDFFC\uE001\uDB00\uDC06§b §e" + player1 + "§b, §e" + player2 + "§b, §e" + player3 + "§b, and §e" + player4 +
                                                                            "§b finished §3" + raid + "thisisatestraid§b");
                                                                    WynnChatMessage.EVENT.invoker().interact(raidFinishedMessage);
                                                                    McUtils.sendLocalMessage(raidFinishedMessage, Text.empty(), false);
                                                                    return Command.SINGLE_SUCCESS;
                                                                })
                                                        ))))));
            }));
        }
    }

    @Override
    public void onConfigUpdate(Config<?> config) {

    }

    private void onWynnMessage(Text message) {
        if (!MinecraftClient.getInstance().isOnThread()) {
            GuildApi.LOGGER.info("not render thread message");
            return;
        }
        Matcher raidMatcher = RAID_PATTERN.matcher(TextUtils.parseStyled(message, TextParseOptions.DEFAULT.withExtractUsernames(true)));
        if (raidMatcher.find()) {
            GuildApi.LOGGER.info("guild raid {} finished", raidMatcher.group("raid"));
            McUtils.sendLocalMessage(Text.literal("Guild raid finished.")
                    .withColor(0x00FF00), Prepend.DEFAULT.get(), false);
        }
    }
}
