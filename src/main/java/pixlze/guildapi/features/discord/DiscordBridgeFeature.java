package pixlze.guildapi.features.discord;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.socket.client.Ack;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.json.JSONArray;
import org.json.JSONObject;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.components.Feature;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.handlers.chat.event.ChatMessageReceived;
import pixlze.guildapi.handlers.discord.event.S2CDiscordEvents;
import pixlze.guildapi.net.SocketIOClient;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.text.FontUtils;
import pixlze.guildapi.utils.text.TextUtils;
import pixlze.guildapi.utils.text.type.TextParseOptions;
import pixlze.guildapi.utils.type.Prepend;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class DiscordBridgeFeature extends Feature {
    private final Pattern GUILD_PATTERN = Pattern.compile("^§[b8c]((\uDAFF\uDFFC\uE006\uDAFF\uDFFF\uE002\uDAFF\uDFFE)|(\uDAFF\uDFFC\uE001\uDB00\uDC06))§[b8c] (?<content>.*)$");
    private final Pattern[] GUILD_WHITELIST_PATTERNS = Stream.of(
            // Basic guild chat message
            "^.*§[38](?<header>.+?)(§[38])?:§[b8] (?<content>.*)$",
            // Guild raid finished
            "^§[e8](?<player1>.*?)§[b8], §[e8](?<player2>.*?)§[b8], §[e8](?<player3>.*?)§[b8], and §[e8](?<player4>.*?)§[b8] finished §[38](?<raid>.*?)§[b8].*$",
            // Giving out resources
            "^§.(?<giver>.*?)(§.)? rewarded §.an Aspect§. to §.(?<receiver>.*?)(§.)?$",
            "^§.(?<giver>.*?)(§.)? rewarded §.a Guild Tome§. to §.(?<receiver>.*?)(§.)?$",
            "^§.(?<giver>.*?)(§.)? rewarded §.1024 Emeralds§. to §.(?<receiver>.*?)(§.)?$",
            // Guild bank
            "^§.(?<username>.+?)§. (?<action>\\w+) §.(?<item>.+?)§. to the Guild Bank \\(§.Everyone§.\\)",
            // Weekly objective
            "^(?<username>.+?) has finished their weekly objective\\.$",
            "^Only (?<time>.+?) left to complete the Weekly Guild Objectives!$",
            // Guild member management
            "^(?<recruiter>.+?) has invited (?<recruit>.+?) to the guild$",
            "^(?<recruiter>.+?) has uninvited (?<recruit>.+?) from the guild$",
            "^(?<recruit>.+?) has joined the guild, say hello!$",
            "^(?<username>.+?) has left the guild$",
            "^(?<kicker>.+?) has kicked (?<kicked>.+?) from the guild$",
            "^(?<setter>.+?) has set (?<set>.+?) guild rank from §.(?<original>\\w+)§. to §.(?<new>\\w+)$",
            // War
            "^The war for (?<territory>.+?) will start in .*$",
            "^Your guild has lost the war for .*$",
            "^The battle has begun!$",
            "^You have taken control of .*$",
            "^\\[\\w+\\] has lost the war!.*$",
            "^\\[\\w+\\] has taken control of .*$"
    ).map(Pattern::compile).toArray(Pattern[]::new);
    private final Pattern[] HR_WHITELIST_PATTERNS = Stream.of(
            // Eco
            "^§.(?<username>.+?)§. set §.(?<bonus>.+?)§. to level §.(?<level>.+?)§. on §.(?<territory>.*)$",
            "^§.(?<username>.+?)§. removed §.(?<changed>.+?)§. from §.(?<territory>.*)$",
            "^§.(?<username>.+?)§. changed §.(?<amount>\\d+) (?<changed>\\w+)§. on §3(?<territory>.*)$",
            "^§.(?<username>.+?)§. applied the loadout §(?<loadout>..+?)§. on §.(?<territory>.*)$",
            "^Territory §.(?<territory>.+?)§. is \\w+ more resources than it can store!$",
            "^Territory §.(?<territory>.+?)§. production has stabilised$",
            "^§.(?<username>.+?)§. applied the loadout §(?<loadout>..+?)§. on §.(?<territory>.*)$",
            // Guild bank
            "^§.(?<username>.+?)§. (?<action>\\w+) §.(?<item>.+?)§. to the Guild Bank \\(§.High Ranked§.\\)$",
            // Guild tome found
            "^§.A Guild Tome§. has been found and added to the Guild Rewards$"
    ).map(Pattern::compile).toArray(Pattern[]::new);
    private SocketIOClient socketIOClient;

    @Override
    public void init() {
        socketIOClient = Managers.Net.socket;

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("discord")
                    .then(ClientCommandManager.argument("message", StringArgumentType.greedyString())
                            .executes((context) -> {
                                String message = StringArgumentType.getString(context, "message");
                                message = message.replaceAll("[\u200C\uE087\uE013\u2064\uE071\uE012\uE000\uE089\uE088\uE07F\uE08B\uE07E\uE080ÁÀ֎]", "");
                                if (message.isBlank()) return 0;
                                if (socketIOClient == null || socketIOClient.discordSocket == null) {
                                    McUtils.sendLocalMessage(Text.literal("Still connecting to chat server...")
                                            .setStyle(Style.EMPTY.withColor(Formatting.YELLOW)), Prepend.DEFAULT.get(), false);
                                    return 0;
                                }
                                socketIOClient.emit(socketIOClient.discordSocket, "discordOnlyWynnMessage", McUtils.playerName() + ": " + message);
                                socketIOClient.emit(socketIOClient.discordSocket, "discordMessage", Map.of("Author", McUtils.playerName(), "Content", message));
                                return Command.SINGLE_SUCCESS;
                            })));
            dispatcher.register(ClientCommandManager.literal("dc").redirect(dispatcher.getRoot().getChild("discord")));

            dispatcher.register(ClientCommandManager.literal("online").executes((context) -> {
                if (socketIOClient == null || socketIOClient.discordSocket == null) {
                    McUtils.sendLocalMessage(Text.literal("Still connecting to chat server...")
                            .setStyle(Style.EMPTY.withColor(Formatting.YELLOW)), Prepend.DEFAULT.get(), false);
                    return 0;
                }
                socketIOClient.emit(socketIOClient.discordSocket, "listOnline", (Ack) args -> {
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
            }));
        });

        ChatMessageReceived.EVENT.register(this::onWynnMessage);
        S2CDiscordEvents.MESSAGE.register(this::onDiscordMessage);
    }

    private void onWynnMessage(Text message) {
        String m = TextUtils.parseStyled(message, TextParseOptions.DEFAULT.withExtractUsernames(true));
        if (GuildApi.isDevelopment()) m = m.replaceAll("&", "§");
        GuildApi.LOGGER.info("received: {}", m);
        Matcher guildMatcher = GUILD_PATTERN.matcher(m);
        if (guildMatcher.find()) {
            if (isGuildMessage(guildMatcher.group("content")))
                socketIOClient.emit(socketIOClient.discordSocket, "wynnMessage", guildMatcher.group("content"));
            else if (isHRMessage(guildMatcher.group("content")))
                socketIOClient.emit(socketIOClient.discordSocket, "hrMessage", guildMatcher.group("content"));
        }
    }

    private void onDiscordMessage(JSONObject message) {
        try {
            GuildApi.LOGGER.info("received discord {}", message.get("Content").toString());
            McUtils.sendLocalMessage(Text.empty().append(FontUtils.BannerPillFont.parseStringWithFill("discord")
                            .fillStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE))).append(" ")
                    .append(Text.literal(message.get("Author").toString())
                            .fillStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE)).append(": "))
                    .append(Text.literal(TextUtils.highlightUser(message.get("Content").toString()))
                            .setStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE))), Prepend.GUILD.getWithStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE)), true);
        } catch (Exception e) {
            GuildApi.LOGGER.info("discord message error: {} {}", e, e.getMessage());
        }
    }

    private boolean isGuildMessage(String message) {
        for (Pattern guildMessagePattern : GUILD_WHITELIST_PATTERNS) {
            if (guildMessagePattern.matcher(message).find()) return true;
        }
        return false;
    }

    private boolean isHRMessage(String message) {
        for (Pattern hrMessagePatter : HR_WHITELIST_PATTERNS) {
            if (hrMessagePatter.matcher(message).find()) return true;
        }
        return false;
    }
}
