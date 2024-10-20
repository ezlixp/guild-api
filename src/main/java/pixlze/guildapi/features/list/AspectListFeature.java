package pixlze.guildapi.features.list;

import com.google.gson.JsonElement;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.handlers.chat.event.ChatMessageReceived;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.text.TextUtils;
import pixlze.guildapi.utils.text.type.TextParseOptions;
import pixlze.guildapi.utils.type.Prepend;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AspectListFeature extends ListFeature {
    private final Pattern ASPECT_MESSAGE_PATTERN = Pattern.compile("^§.((\uDAFF\uDFFC\uE001\uDB00\uDC06)|(\uDAFF\uDFFC\uE006\uDAFF\uDFFF\uE002\uDAFF\uDFFE))§. §.(?<giver>.*?)(§" +
            ".)? rewarded §.an Aspect§. to §.(?<receiver>.*?)(§.)?$");

    public AspectListFeature() {
        super("aspect", "aspects", (listItem) -> Text.literal(listItem.get("username")
                        .getAsString()).append(": ")
                .append(listItem.get("aspects").getAsString())
                .setStyle(Style.EMPTY.withColor(Formatting.WHITE)));
    }

    @Override
    public void init() {
        ChatMessageReceived.EVENT.register(this::onWynnMessage);
        super.registerCommands(List.of(ClientCommandManager.literal("search").executes((context) -> {
                    search(McUtils.playerName());
                    return Command.SINGLE_SUCCESS;
                })
                .then(ClientCommandManager.argument("username", StringArgumentType.word())
                        .executes((context) -> {
                            search(StringArgumentType.getString(context, "username"));
                            return Command.SINGLE_SUCCESS;
                        })
                )));

    }

    private void onWynnMessage(Text message) {
        if (!MinecraftClient.getInstance().isOnThread()) {
            GuildApi.LOGGER.info("not render thread message");
            return;
        }
        String aspectMessage = TextUtils.parseStyled(message, TextParseOptions.DEFAULT.withExtractUsernames(true));
        Matcher aspectMatcher = ASPECT_MESSAGE_PATTERN.matcher(aspectMessage);
        if (aspectMatcher.find()) {
            GuildApi.LOGGER.info("{} gave an aspect to {}", aspectMatcher.group("giver"), aspectMatcher.group("receiver"));
        }
    }

    private void search(String username) {
        CompletableFuture<JsonElement> response = Managers.Net.guild.get("aspects/" + username);
        response.whenCompleteAsync((res, exception) -> {
            if (exception == null && res != null) {
                McUtils.sendLocalMessage(Text.literal(res.getAsJsonObject()
                        .get("username").getAsString() + " is owed " + res.getAsJsonObject()
                        .get("aspects").getAsString() + " aspects.").withColor(0xFFFFFF), Prepend.DEFAULT.get(), false);
            }
        });
    }
}
