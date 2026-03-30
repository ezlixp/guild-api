package pixlze.guildapi.features.discord;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.MutableText;
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
import pixlze.guildapi.core.config.SyncConfigurable;
import pixlze.guildapi.core.features.FeatureState;
import pixlze.guildapi.core.handlers.chat.event.ChatMessageReceived;
import pixlze.guildapi.core.handlers.discord.event.S2CSocketEvents;
import pixlze.guildapi.discord.type.Message;
import pixlze.guildapi.mc.mixin.accessors.SystemToastInvoker;
import pixlze.guildapi.models.Models;
import pixlze.guildapi.models.guildMessage.type.GuildMessage;
import pixlze.guildapi.utils.ColourUtils;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.text.TextUtils;
import pixlze.guildapi.utils.text.type.TextParseOptions;
import pixlze.guildapi.utils.type.Prepend;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class DiscordBridgeFeature extends Feature {
    public DiscordBridgeFeature() {
        super("Discord Bridging");
    }

    @Configurable
    public final Config<Boolean> useGui = new Config<>(false);

    @Configurable
    public final Config<String> highlight = new Config<>("");

    /**
     * 0 - online
     * 1 - busy (not implemented)
     * 2 - dnd (not implemented)
     * 3 - appear offline
     */
    @SyncConfigurable(syncUri = "user/onlineStatus/", cycleLength = 4)
    public final Config<Integer> onlineStatus = new Config<>(0);

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
        if (Managers.Feature.getFeatureState(this) == FeatureState.DISABLED)
            return;
        String m = TextUtils.parseStyled(message, TextParseOptions.DEFAULT.withExtractUsernames(true));
        if (GuildApi.isDevelopment()) m = m.replaceAll("&", "§");
        GuildApi.LOGGER.info("received: {}", m);
        // !m.contains("\uE003") is to ensure private messages do not get matched as base guild messages if they have the line instead of the badge
        if (Managers.DiscordSocket.onWorld && !m.contains("\uE003")) {
            if (Models.GuildMessage.isGuildMessage(m) != null)
                Managers.DiscordSocket.emit("wynnMessage", Models.GuildMessage.getContent(m));
            else if (Models.GuildMessage.isHighRankMessage(m) != null)
                Managers.DiscordSocket.emit("hrMessage", Models.GuildMessage.getContent(m));
        }
    }

    private void onDiscordMessage(JSONObject message) {
        if (Managers.Feature.getFeatureState(this) == FeatureState.DISABLED) {
            GuildApi.LOGGER.warn("received discord message with disabled feature.");
            return;
        }
        String username = null, content, discord;
        try {
            if (message.has("McUsername"))
                username = message.get("McUsername").toString();
            content = message.get("Content").toString();
            discord = message.get("DiscordUsername").toString();
            if (discord.equals("@none")) discord = "";
        } catch (Exception e) {
            GuildApi.LOGGER.info("discord message extract: {} {}", e, e.getMessage());
            return;
        }

        Message m = new Message(username, discord, content, false, this::highlightMessage);
        if (!useGui.getValue()) {
            McUtils.sendLocalMessage(m.get(), Prepend.GUILD.getWithStyle(ColourUtils.DARK_PURPLE), true);
        } else {
            TextRenderer textRenderer = McUtils.mc().textRenderer;
            Objects.requireNonNull(textRenderer);
            List<OrderedText> lines = m.getContentLines((int) (McUtils.mc().getWindow()
                    .getScaledWidth() * 0.25)).stream().map(MutableText::asOrderedText).collect(Collectors.toCollection(ArrayList::new));
            int width = Math.max(50, lines.stream().mapToInt(textRenderer::getWidth).max()
                    .orElse((int) (McUtils.mc().getWindow().getScaledWidth() * 0.25)));
            McUtils.mc().getToastManager()
                    .add(SystemToastInvoker.create(SystemToast.Type.PERIODIC_NOTIFICATION, m.getAuthor(), lines, width + 30));
        }

        Managers.Discord.newMessage(username, discord, content, false, true);
    }

    private void onWynnMirror(String message) {
        if (Managers.Feature.getFeatureState(this) == FeatureState.DISABLED) {
            GuildApi.LOGGER.warn("received wynn mirror with disabled feature.");
            return;
        }
        if (!Managers.DiscordSocket.onWorld) {
            Matcher t = GuildMessage.BASIC.getMatcher(message);
            if (t != null) {
                String pill = t.group("pill");
                String leftover = message.substring(pill.length());
                Text mirrored = Text.empty()
                        .append(Text.literal(pill).setStyle(TextUtils.fontOf(Identifier.of("banner/pill"))))
                        .append(Text.literal(leftover).setStyle(Style.EMPTY));
                McUtils.sendLocalMessage(mirrored, Prepend.EMPTY.get(), false);
                Handlers.Chat.postChatLine(mirrored);
                Managers.Discord.newMessage(t.group("header"), t.group("content"), true, true);
            } else {
                McUtils.sendLocalMessage(Text.literal(message), Prepend.EMPTY.get(), false);
                Handlers.Chat.postChatLine(Text.literal(message));
                Managers.Discord.newMessage("⚠ Info", message, true, true);
            }
        }
    }

    public String highlightMessage(String message) {
        if (message == null) return null;
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
