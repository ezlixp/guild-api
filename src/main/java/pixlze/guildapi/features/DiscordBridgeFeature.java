package pixlze.guildapi.features;

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
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.handlers.chat.event.ChatMessageReceived;
import pixlze.guildapi.net.SocketIOClient;
import pixlze.guildapi.net.event.NetEvents;
import pixlze.guildapi.net.type.Api;
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
    private final Pattern[] GUILD_WHITELIST_PATTERNS = Stream.of("^.*§[38](?<header>.+?)(§[38])?:§[b8] (?<content>.*)$",
            "^§[e8](?<player1>.*?)§[b8], §[e8](?<player2>.*?)§[b8], §[e8](?<player3>.*?)§[b8], and §[e8](?<player4>.*?)§[b8] finished §[38](?<raid>.*?)§[b8].*$",
            "^§.(?<giver>.*?)(§.)? rewarded §.an Aspect§. to §.(?<receiver>.*?)(§.)?$",
            "^§.(?<giver>.*?)(§.)? rewarded §.a Guild Tome§. to §.(?<receiver>.*?)(§.)?$",
            "^§.(?<giver>.*?)(§.)? rewarded §.1024 Emeralds§. to §.(?<receiver>.*?)(§.)?$",
            "^§.(?<username>.+?)§. (?<action>\\w+) §.(?<item>.+?)§. to the Guild Bank \\(§.Everyone§.\\)",
            "^(?<username>.+?) has finished their weekly objective.$",
            "^§.(?<recruiter>.+?)§. has invited (?<recruit>.+?) to the guild$",
            "^(?<recruit>.+?) has joined the guild, say hello!$",
            "^(?<username>.+?) has left the guild$",
            "^§.(?<kicker>.+?)§. has kicked §.(?<kicked>.+?)§. from the guild$",
            "^(?<setter>.+?) has set (?<set>.+?) guild rank from §.(?<original>\\w+)§. to §.(?<new>\\w+)$",
            "^Only (?<time>.+?) left to complete the Weekly Guild Objectives!$").map(Pattern::compile).toArray(Pattern[]::new);
    private final Pattern[] HR_WHITELIST_PATTERNS = Stream.of("^§.(?<username>.+?)§. set §.(?<bonus>.+?)§. to level §.(?<level>.+?)§. on §.(?<territory>.*)$",
            "^§.(?<username>.+?)§. removed §.(?<changed>.+?)§. from §.(?<territory>.*)$",
            "^§.(?<username>.+?)§. changed §.(?<amount>\\d+) (?<changed>\\w+)§. on §3(?<territory>.*)$",
            "^§.(?<username>.+?)§. applied the loadout §(?<loadout>..+?)§. on §.(?<territory>.*)$",
            "^Territory §.(?<territory>.+?)§. is \\w+ more resources than it can store!$",
            "^Territory §.(?<territory>.+?)§. production has stabilised$",
            "^§.(?<username>.+?)§. applied the loadout §(?<loadout>..+?)§. on §.(?<territory>.*)$",
            "^§.(?<username>.+?)§. (?<action>\\w+) §.(?<item>.+?)§. to the Guild Bank \\(§.High Ranked§.\\)$",
            "^§.A Guild Tome§. has been found and added to the Guild Rewards$").map(Pattern::compile).toArray(Pattern[]::new);
    private SocketIOClient socketIOClient;
    private boolean loaded = false;

    @Override
    public void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("discord").then(ClientCommandManager.argument("message", StringArgumentType.greedyString()).executes((context) -> {
                String message = StringArgumentType.getString(context, "message");
                message = message.replaceAll("[\u200C\uE087\uE013\u2064\uE071\uE012\uE000\uE089\uE088\uE07F\uE08B\uE07E\uE080ÁÀ֎]", "");
                if (message.isBlank()) return 0;
                if (socketIOClient != null) {
                    socketIOClient.emit(socketIOClient.discordSocket, "discordOnlyWynnMessage", McUtils.playerName() + ": " + message);
                    socketIOClient.emit(socketIOClient.discordSocket, "discordMessage", Map.of("Author", McUtils.playerName(), "Content", message));
                } else {
                    McUtils.sendLocalMessage(Text.literal("Still connecting to chat server...").setStyle(Style.EMPTY.withColor(Formatting.YELLOW)), Prepend.DEFAULT.get(), false);
                }
                return 0;
            })));
            dispatcher.register(ClientCommandManager.literal("dc").redirect(dispatcher.getRoot().getChild("discord")));

            dispatcher.register(ClientCommandManager.literal("online").executes((context) -> {
                if (socketIOClient != null) {
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
                } else {
                    McUtils.sendLocalMessage(Text.literal("Still connecting to chat server...").setStyle(Style.EMPTY.withColor(Formatting.YELLOW)), Prepend.DEFAULT.get(), false);
                }
                return 0;
            }));
        });
        NetEvents.LOADED.register(this::onApiLoaded);
    }

    private void onApiLoaded(Api api) {
        if (api.getClass().equals(SocketIOClient.class) && !loaded) {
            loaded = true;
            ChatMessageReceived.EVENT.register(this::onWynnMessage);
            socketIOClient = Managers.Net.socket;
            socketIOClient.addDiscordListener("discordMessage", this::onDiscordMessage);
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

    private void onWynnMessage(Text message) {
        String m = TextUtils.parseStyled(message, TextParseOptions.DEFAULT.withExtractUsernames(true));
        if (GuildApi.isDevelopment()) m = m.replaceAll("&", "§");
        GuildApi.LOGGER.info("received: {}", m);
        Matcher guildMatcher = GUILD_PATTERN.matcher(m);
        if (guildMatcher.find()) {
            if (isGuildMessage(m))
                socketIOClient.emit(socketIOClient.discordSocket, "wynnMessage", guildMatcher.group("content"));
            else if (isHRMessage(m))
                socketIOClient.emit(socketIOClient.discordSocket, "hrMessage", guildMatcher.group("content"));
        }
    }

    private void onDiscordMessage(Object[] args) {
        if (args[0] instanceof JSONObject data) {
            try {
                GuildApi.LOGGER.info("received discord {}", data.get("Content").toString());
                if (data.get("Content").toString().isBlank()) return;
                McUtils.sendLocalMessage(Text.empty().append(FontUtils.BannerPillFont.parseStringWithFill("discord").fillStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE))).append(" ").append(Text.literal(data.get("Author").toString()).fillStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE)).append(": ")).append(Text.literal(TextUtils.highlightUser(data.get("Content").toString())).setStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE))), Prepend.GUILD.getWithStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE)), true);
            } catch (Exception e) {
                GuildApi.LOGGER.info("discord message error: {} {}", e, e.getMessage());
            }
        } else {
            GuildApi.LOGGER.info("malformed discord message: {}", args);
        }
    }
}
