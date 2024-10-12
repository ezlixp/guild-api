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
    private final Pattern GUILD_PATTERN = Pattern.compile("^§[b8]((\uDAFF\uDFFC\uE006\uDAFF\uDFFF\uE002\uDAFF\uDFFE)|(\uDAFF\uDFFC\uE001\uDB00\uDC06))§[b8] (?<content>.*)$");
    private final Pattern[] CONFLICT_PATTERNS = Stream.of("Sorry, you can't teleport... Try moving away from blocks.", "^[a-zA-Z0-9_]{2,16}:.*$", "^You .*$", "To rename a pet, " +
                    "use /renamepet. To rename an item, use " + "/renameitem.", "You need to " +
                    "be holding a crafted item to rename it!", "/renamepet <pet-name>", "This command is VIP+ only! Buy VIP+ at §cwynncraft.com/store", "This command is " +
                    "HERO " +
                    "only! Buy HERO at §cwynncraft.com/store", "Invalid command... Type /help for a list of commands", "/toggle [swears/blood/insults/autojoin/music/vet" +
                    "/war/guildjoin/attacksound/rpwarning/100/sb/autotracking/pouchmsg/combatbar/ghosts/popups/guildpopups/friendpopups/beacon/outlines/bombbell" +
                    "/pouchpickup" +
                    "/queststartbeacon/publicProfile]", "You must specify a ghost limit to use.", "You're not a vet... Sorry!", "^Did you mean .*$", "^Your .*$", "^Party" +
                    " .*$", "^Sorry, .*$")
            .map(Pattern::compile).toArray(Pattern[]::new);
    private SocketIOClient socketIOClient;
    private boolean loaded = false;

    @Override
    public void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("discord").then(ClientCommandManager.argument("message", StringArgumentType.greedyString()).executes((context) -> {
                String message = StringArgumentType.getString(context, "message");
                message = message.replaceAll("[\u200C\uE087\uE013\u2064\uE071\uE012\uE000\uE089\uE088" + "\uE07F\uE08B\uE07E\uE080ÁÀ֎]", "");
                if (message.isBlank()) return 0;
                if (socketIOClient != null) {
                    socketIOClient.emit(socketIOClient.discordSocket, "discordOnlyWynnMessage", "[Discord Only] " + McUtils.playerName() + ": " + message);
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

    private void onWynnMessage(Text message) {
        String m = TextUtils.parseStyled(message, TextParseOptions.DEFAULT.withExtractUsernames(true));
        if (GuildApi.isDevelopment()) m = m.replaceAll("&", "§");
        GuildApi.LOGGER.info("received: {}", m);
        for (Pattern conflict : CONFLICT_PATTERNS) {
            if (conflict.matcher(m).find()) {
                GuildApi.LOGGER.warn("cancelled emit for conflicting message: {}", m);
                return;
            }
        }
        Matcher guildMatcher = GUILD_PATTERN.matcher(m);
        if (guildMatcher.find()) {
            socketIOClient.emit(socketIOClient.discordSocket, "wynnMessage", guildMatcher.group("content"));
        }
    }

    private void onDiscordMessage(Object[] args) {
        if (args[0] instanceof JSONObject data) {
            try {
                GuildApi.LOGGER.info("received discord {}", data.get("Content").toString());
                if (data.get("Content").toString().isBlank()) return;
                McUtils.sendLocalMessage(Text.empty().append(FontUtils.BannerPillFont.parseStringWithFill("discord").fillStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE)))
                        .append(" ").append(Text.literal(data.get("Author").toString()).fillStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE)).append(": "))
                        .append(Text.literal(TextUtils.highlightUser(data.get("Content").toString()))
                                .setStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE))), Prepend.GUILD.getWithStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE)), true);
            } catch (Exception e) {
                GuildApi.LOGGER.info("discord message error: {} {}", e, e.getMessage());
            }
        }
    }
}
