package pixlze.guildapi.features.discord;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.json.JSONObject;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.components.Feature;
import pixlze.guildapi.core.components.Handlers;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.core.config.Config;
import pixlze.guildapi.core.config.Configurable;
import pixlze.guildapi.core.features.FeatureState;
import pixlze.guildapi.core.handlers.chat.event.ChatMessageReceived;
import pixlze.guildapi.core.handlers.discord.event.S2CSocketEvents;
import pixlze.guildapi.discord.DiscordMessageManager;
import pixlze.guildapi.mc.mixin.accessors.SystemToastInvoker;
import pixlze.guildapi.utils.ColourUtils;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.text.TextUtils;
import pixlze.guildapi.utils.text.type.TextParseOptions;
import pixlze.guildapi.utils.type.Prepend;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class DiscordBridgeFeature extends Feature {
    private final Pattern GUILD_PATTERN = Pattern.compile("^§[b8c]((\uDAFF\uDFFC\uE006\uDAFF\uDFFF\uE002\uDAFF\uDFFE)|(\uDAFF\uDFFC\uE001\uDB00\uDC06))§[b8c] (?<content>.*)$");
    private final Pattern[] GUILD_WHITELIST_PATTERNS = Stream.of(
            // Basic guild chat message
            "^(?<pill>.*)§[38](?<header>.+?)(§[38])?:§[b8] (?<content>.*)$",
            // Guild raid finished
            "^§[e8](?<player1>.*?)§[b8], §[e8](?<player2>.*?)§[b8], §[e8](?<player3>.*?)§[b8], and §[e8](?<player4>.*?)§[b8] finished §[38](?<raid>.*?)§[b8].*$",
            // Giving out resources
            "^§.(?<giver>.*?)(§.)? rewarded §.an Aspect§. to §.(?<receiver>.*?)(§.)?$",
            "^§.(?<giver>.*?)(§.)? rewarded §.a Guild Tome§. to §.(?<receiver>.*?)(§.)?$",
            "^§.(?<giver>.*?)(§.)? rewarded §.1024 Emeralds§. to §.(?<receiver>.*?)(§.)?$",
            // Guild bank
            "^§.(?<username>.+?)§. (?<action>\\w+) §.(?<item>.+?)§. (?:to|from) the Guild Bank \\(§.Everyone§.\\)",
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
            "^\\[\\w+\\] has taken control of .*$",
            // Guild season
            "^The current guild season will end in .*$",
            "^The last standing territories you control once it ends will grant you 2048² each!$",
            // Misc
            "^§.(?<username>.+?) has started boosting the guild$"
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
            "^§.(?<username>.+?)§. (?<action>\\w+) §.(?<item>.+?)§. (?:to|from) the Guild Bank \\(§.High Ranked§.\\)$",
            // Guild tome found
            "^§.A Guild Tome§. has been found and added to the Guild Rewards$",

            // Unsure
            "^(?<username>.+?) from (?<guild>.+?) is requesting to be allied$",
            "^(?<username>.+?) sent (?<guild>.+?) a request to be allied$",
            "^(?<username>.+?) rejected (?<guild>.+?) alliance request$",

            "^(?<guild1>.+?) formed an alliance with (?<guild2>.?)$",
            "^(?<username>.+?) revoked the alliance with (?<guild>.*?)$"
    ).map(Pattern::compile).toArray(Pattern[]::new);

    public DiscordBridgeFeature() {
        super("Discord Bridging");
    }

    @Configurable
    public final Config<Boolean> useGui = new Config<>(false);

    @Configurable
    public final Config<String> highlight = new Config<>("");

    @Override
    public void init() {
        ChatMessageReceived.EVENT.register(this::onWynnMessage);
        S2CSocketEvents.DISCORD_MESSAGE.register(this::onDiscordMessage);
        S2CSocketEvents.WYNN_MIRROR.register(this::onWynnMirror);
    }

    @Override
    public void onConfigUpdate(Config<?> config) {

    }

    @Override
    public void onEnabled() {
        Managers.DiscordSocket.initSocket();
    }

    @Override
    public void onDisabled() {
        Managers.DiscordSocket.disable();
        Managers.Discord.clearMessages();
    }

    private void onWynnMessage(Text message) {
        if (Managers.Feature.getFeatureState(this) == FeatureState.DISABLED) return;
        String m = TextUtils.parseStyled(message, TextParseOptions.DEFAULT.withExtractUsernames(true));
        if (GuildApi.isDevelopment()) m = m.replaceAll("&", "§");
        GuildApi.LOGGER.info("received: {}", m);
        Matcher guildMatcher = GUILD_PATTERN.matcher(m);
        if (Managers.DiscordSocket.onWorld && !m.contains("\uE003") && guildMatcher.find()) {
            if (isGuildMessage(guildMatcher.group("content")))
                Managers.DiscordSocket.emit("wynnMessage", guildMatcher.group("content"));
            else if (isHRMessage(guildMatcher.group("content")))
                Managers.DiscordSocket.emit("hrMessage", guildMatcher.group("content"));
        }
    }

    private void onDiscordMessage(JSONObject message) {
        if (Managers.Feature.getFeatureState(this) == FeatureState.DISABLED) {
            GuildApi.LOGGER.warn("received discord message with disabled feature.");
            return;
        }
        String username, content, discord;
        try {
             username = message.get("McUsername").toString();
             content = message.get("Content").toString();
             discord = message.get("DiscordUsername").toString();
             if (discord.equals("@none")) discord = "";
        } catch (Exception e) {
            GuildApi.LOGGER.info("discord message extract: {} {}", e, e.getMessage());
            return;
        }

        String combined = Managers.Discord.addDiscord(username, discord);
        if (!useGui.getValue()) {
                McUtils.sendLocalMessage(Managers.Discord.toDiscordMessage(highlightMessage(Managers.Discord.parse(combined)),  highlightMessage(content)), Prepend.GUILD.getWithStyle(ColourUtils.DARK_PURPLE), true);
        } else {
                TextRenderer textRenderer = McUtils.mc().textRenderer;
                List<OrderedText> lines = textRenderer.wrapLines(Text.literal(highlightMessage(content)), (int) (McUtils.mc().getWindow()
                        .getScaledWidth() * 0.25));
                Objects.requireNonNull(textRenderer);
                int width = Math.max(50, lines.stream().mapToInt(textRenderer::getWidth).max()
                        .orElse((int) (McUtils.mc().getWindow().getScaledWidth() * 0.25)));
                McUtils.mc().getToastManager()
                        .add(SystemToastInvoker.create(SystemToast.Type.PERIODIC_NOTIFICATION, Text.literal(highlightMessage(Managers.Discord.parse(combined))), lines, width + 30));
        }

        Managers.Discord.newMessage(username, discord, content, true, DiscordMessageManager.DISCORD_MESSAGE);
    }

    private void onWynnMirror(String message) {
        if (Managers.Feature.getFeatureState(this) == FeatureState.DISABLED) {
            GuildApi.LOGGER.warn("received wynn mirror with disabled feature.");
            return;
        }
        if (!Managers.DiscordSocket.onWorld || GuildApi.isTesting()) {
            Matcher matcher = GUILD_WHITELIST_PATTERNS[0].matcher(message);
            if (matcher.find()) {
                String pill = matcher.group("pill");
                String leftover = message.substring(pill.length());
                Text mirrored = Text.empty()
                        .append(Text.literal(pill).setStyle(Style.EMPTY.withFont(Identifier.of("banner/pill"))))
                        .append(Text.literal(leftover).setStyle(Style.EMPTY));
                McUtils.sendLocalMessage(mirrored, Prepend.GUILD.get(), true);
                Handlers.Chat.postChatLine(mirrored);
                Managers.Discord.newMessage(matcher.group("header"), matcher.group("content"), true, DiscordMessageManager.GUILD_MESSAGE);
            } else {
                McUtils.sendLocalMessage(Text.literal(message), Prepend.GUILD.get(), true);
                Handlers.Chat.postChatLine(Text.literal(message));
                Managers.Discord.newMessage("⚠ Info",  message, true, DiscordMessageManager.GUILD_MESSAGE);
            }
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

    public String highlightMessage(String message) {
        String[] phrases = highlight.getValue().split(",");
        int[] diff = new int[message.length() + 1];
        for (String phrase : phrases) {
            if (phrase.isBlank()) continue;
            int index = 0;
            while ((index = message.indexOf(phrase, index)) != -1) {
                diff[index]++;
                diff[index + phrase.length()]--;
                index += phrase.length();
            }
        }

        StringBuilder out = new StringBuilder();
        boolean sectioned = false;
        int sum = 0;
        for (int i = 0; i < message.length(); i++) {
            sum += diff[i];
            if (sum > 0 && !sectioned) {
                sectioned = true;
                out.append("§e");
            } else if (sum <= 0 && sectioned) {
                sectioned = false;
                out.append("§r");
            }
            out.append(message.charAt(i));
        }
        return out.toString();
    }
}
