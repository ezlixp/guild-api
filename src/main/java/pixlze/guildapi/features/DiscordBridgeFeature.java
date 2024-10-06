package pixlze.guildapi.features;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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

public class DiscordBridgeFeature extends Feature {
    private final Pattern GUILD_PATTERN = Pattern.compile("^§[b8]((\uDAFF\uDFFC\uE006\uDAFF\uDFFF\uE002\uDAFF\uDFFE)|(\uDAFF\uDFFC\uE001\uDB00\uDC06))§[b8] (?<content>.*)$");
    private final Pattern PARTY_CONFLICT_PATTERN = Pattern.compile("^§8\uDAFF\uDFFC\uE001\uDB00\uDC06§8 [a-zA-Z0-9_]{2,16}:.*$");

    @Override
    public void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            LiteralArgumentBuilder<FabricClientCommandSource> base = ClientCommandManager.literal("discord")
                    .then(ClientCommandManager.argument("message", StringArgumentType.greedyString())
                            .executes((context) -> {
                                String message = StringArgumentType.getString(context, "message");
                                GuildApi.LOGGER.info("raw message: {}", message);
                                message = message.replaceAll("[\u200C\uE087\uE013\u2064\uE071\uE012\uE000\uE089\uE088\uE07F\uE08B\uE07E\uE080ÁÀ֎]", "");
                                if (message.isBlank()) return 0;
                                if (Managers.Net.getApi("socket", SocketIOClient.class) != null) {
                                    Managers.Net.getApi("socket", SocketIOClient.class)
                                            .discordEmit("discordOnlyWynnMessage", "[Discord Only] " + McUtils.playerName() + ": " + message);
                                    Managers.Net.getApi("socket", SocketIOClient.class)
                                            .discordEmit("discordMessage", Map.of("Author", McUtils.playerName(), "Content", message));

                                } else {
                                    McUtils.sendLocalMessage(Text.literal("Still connecting to server...")
                                            .setStyle(Style.EMPTY.withColor(Formatting.YELLOW)), Prepend.DEFAULT.get());
                                }
                                return 0;
                            })
                    );
            dispatcher.register(base);
            dispatcher.register(ClientCommandManager.literal("dc").redirect(dispatcher.getRoot().getChild("discord")));
        });
        NetEvents.LOADED.register(this::onApiLoaded);
    }

    private void onApiLoaded(Api api) {
        if (api.getClass().equals(SocketIOClient.class)) {
            ChatMessageReceived.EVENT.register(this::onWynnMessage);
            Managers.Net.getApi("socket", SocketIOClient.class)
                    .addDiscordListener("discordMessage", this::onDiscordMessage);

        }
    }

    private void onWynnMessage(Text message) {
        String m = TextUtils.parseStyled(message, TextParseOptions.DEFAULT.withExtractUsernames(true));
        GuildApi.LOGGER.info("received: {}", m);
        Matcher guildMatcher = GUILD_PATTERN.matcher(m);
        Matcher partyConflictMatcher = PARTY_CONFLICT_PATTERN.matcher(m);
        if (guildMatcher.find() && !partyConflictMatcher.find()) {
            Managers.Net.getApi("socket", SocketIOClient.class)
                    .discordEmit("wynnMessage", guildMatcher.group("content"));
        }
    }

    private void onDiscordMessage(Object[] args) {
        if (args[0] instanceof JSONObject data) {
            try {
                GuildApi.LOGGER.info("received discord {}", data.get("Content").toString());
                if (data.get("Content").toString().isBlank()) return;
                McUtils.sendLocalMessage(Text.empty()
                        .append(FontUtils.BannerPillFont.parseStringWithFill("discord")
                                .fillStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE)))
                        .append(" ")
                        .append(Text.literal(data.get("Author").toString())
                                .fillStyle(Style.EMPTY.withColor(Formatting.DARK_AQUA).withBold(true))
                                .append(": "))
                        .append(Text.literal(data.get("Content").toString())
                                .setStyle(Style.EMPTY.withColor(Formatting.AQUA))), Prepend.GUILD.getWithStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE)));
            } catch (Exception e) {
                GuildApi.LOGGER.info("discord message error: {} {}", e, e.getMessage());
            }
        }
    }
}
