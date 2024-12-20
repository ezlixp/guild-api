package pixlze.guildapi.features.guildresources;

import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.features.type.ListFeature;
import pixlze.guildapi.handlers.chat.event.ChatMessageReceived;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.NetUtils;
import pixlze.guildapi.utils.text.TextUtils;
import pixlze.guildapi.utils.text.type.TextParseOptions;
import pixlze.guildapi.utils.type.Prepend;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AspectListFeature extends ListFeature {
    private final Pattern ASPECT_MESSAGE_PATTERN = Pattern.compile("^§.((\uDAFF\uDFFC\uE001\uDB00\uDC06)|(\uDAFF\uDFFC\uE006\uDAFF\uDFFF\uE002\uDAFF\uDFFE))§. §.(?<giver>.*?)(§" +
            ".)? rewarded §.an Aspect§. to §.(?<receiver>.*?)(§.)?$");

    public AspectListFeature() {
        super("aspect", "aspects", (listItem) -> Text.literal(listItem.getAsJsonObject().get("username")
                        .getAsString()).append(": ")
                .append(listItem.getAsJsonObject().get("aspects").getAsString())
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
        Managers.Net.guild.get("aspects/" + username).whenCompleteAsync((res, exception) -> {
            try {
                NetUtils.applyDefaultCallback(res, exception, (response) -> {
                    JsonObject resObject = response.getAsJsonObject();
                    McUtils.sendLocalMessage(Text.literal(resObject.get("username").getAsString() + " is owed " + resObject
                            .get("aspects").getAsString() + " aspects.").withColor(0xFFFFFF), Prepend.DEFAULT.get(), false);
                }, (error) -> {
                    if (error.equals("Specified user could not be found in aspect list."))
                        McUtils.sendLocalMessage(Text.literal("§eCould not find \"" + username + "\" in the aspect queue."), Prepend.DEFAULT.get(), false);
                    else
                        McUtils.sendLocalMessage(Text.literal("§cCould not fetch aspect list. Reason: " + error), Prepend.DEFAULT.get(), false);
                });
            } catch (Exception e) {
                McUtils.sendLocalMessage(Text.literal("§cSomething went wrong. Check logs for more details."), Prepend.DEFAULT.get(), false);
                GuildApi.LOGGER.error("aspects search error: {} {}", e, e.getMessage());
            }
        });
    }
}
