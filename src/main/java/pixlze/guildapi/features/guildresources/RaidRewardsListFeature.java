package pixlze.guildapi.features.guildresources;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.features.type.ListFeature;
import pixlze.guildapi.handlers.chat.event.ChatMessageReceived;
import pixlze.guildapi.mc.event.WynnChatMessage;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.NetUtils;
import pixlze.guildapi.utils.text.TextUtils;
import pixlze.guildapi.utils.text.type.TextParseOptions;
import pixlze.guildapi.utils.type.Prepend;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RaidRewardsListFeature extends ListFeature {
    private static final Pattern ASPECT_MESSAGE_PATTERN = Pattern.compile("^§.((\uDAFF\uDFFC\uE001\uDB00\uDC06)|(\uDAFF\uDFFC\uE006\uDAFF\uDFFF\uE002\uDAFF\uDFFE))§. §.(?<giver>.*?)(§" +
            ".)? rewarded §.an Aspect§. to §.(?<receiver>.*?)(§.)?$");
    private static final String ENDPOINT = "guilds/raids/rewards/";

    public RaidRewardsListFeature() {
        super("raid", ENDPOINT, RaidRewardsListFeature::formatLine, "raids");
    }

    private static MutableText formatLine(JsonElement listItem, String sortMember) {
        List<Pair<MutableText, String>> components = new java.util.ArrayList<>(List.of(
                new Pair<>(Text.literal(listItem.getAsJsonObject().get("raids").getAsString()).append(" raids"), "raids"),
                new Pair<>(Text.literal(listItem.getAsJsonObject().get("aspects").getAsString()).append(" aspects"), "aspects"),
                new Pair<>(Text.literal(String.format("%.2f", listItem.getAsJsonObject().get("liquidEmeralds").getAsDouble())).append(" ¼²"), "liquidEmeralds")
        ));
        components.sort((a, b) -> {
            if (a.getRight().equals(sortMember)) return -1;
            if (b.getRight().equals(sortMember)) return 1;
            return 0;
        });
        MutableText out = Text.literal(listItem.getAsJsonObject().get("username")
                .getAsString()).append(": ");
        for (int i = 0; i < components.size() - 1; i++) {
            out.append(components.get(i).getLeft()).append(" | ");
        }
        out.append(components.getLast().getLeft());
        return out;
    }

    @Override
    public void init() {
        ChatMessageReceived.EVENT.register(this::onWynnMessage);
        super.registerCommands(List.of(
                ClientCommandManager.literal("search").executes((context) -> {
                            search(McUtils.playerName());
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("username", StringArgumentType.word())
                                .executes((context) -> {
                                    search(StringArgumentType.getString(context, "username"));
                                    return Command.SINGLE_SUCCESS;
                                })
                        ),
                ClientCommandManager.literal("sort")
                        .then(ClientCommandManager.literal("raids").executes(context -> {
                            super.setSortMember("raids");
                            McUtils.sendLocalMessage(Text.literal("Successfully set raid list to sort by \"raids\"."), Prepend.DEFAULT.get(), false);
                            return Command.SINGLE_SUCCESS;
                        })).then(ClientCommandManager.literal("aspects").executes(context -> {
                            super.setSortMember("aspects");
                            McUtils.sendLocalMessage(Text.literal("Successfully set raid list to sort by \"aspects\"."), Prepend.DEFAULT.get(), false);
                            return Command.SINGLE_SUCCESS;
                        })).then(ClientCommandManager.literal("emeralds").executes(context -> {
                            super.setSortMember("liquidEmeralds");
                            McUtils.sendLocalMessage(Text.literal("Successfully set raid list to sort by \"emeralds\"."), Prepend.DEFAULT.get(), false);
                            return Command.SINGLE_SUCCESS;
                        }))
        ));

        if (GuildApi.isDevelopment()) {
            ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> {
                dispatcher.register(ClientCommandManager.literal("aspect").then(ClientCommandManager.argument("username", StringArgumentType.word()).executes((context) -> {
                    String username = StringArgumentType.getString(context, "username");
                    Text aspectGivenMessage = Text.literal("§b\uDAFF\uDFFC\uE006\uDAFF\uDFFF\uE002\uDAFF\uDFFE§b §epixlze§b rewarded §ean Aspect§b to §e" + username);
                    WynnChatMessage.EVENT.invoker().interact(aspectGivenMessage);
                    McUtils.sendLocalMessage(aspectGivenMessage, Text.empty(), false);
                    return Command.SINGLE_SUCCESS;
                })));
            }));
        }
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
