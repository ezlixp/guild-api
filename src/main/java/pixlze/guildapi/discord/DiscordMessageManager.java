package pixlze.guildapi.discord;

import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.components.Manager;
import pixlze.guildapi.screens.discord.widgets.DiscordChatWidget;
import pixlze.guildapi.utils.ColourUtils;
import pixlze.guildapi.utils.text.FontUtils;

import java.util.ArrayList;
import java.util.List;

public class DiscordMessageManager extends Manager {
    public static final String GUILD_MESSAGE = "\uD83C\uDD56";
    public static final String DISCORD_MESSAGE = "\uD83C\uDD53";
    private final List<Pair<String, String>> messages = new ArrayList<>();
    // 🅖 first means guild 🅓 first means discord
    private final ArrayList<Integer> unconfirmedIndex = new ArrayList<>();
    private DiscordChatWidget curDiscordChat;

    public DiscordMessageManager() {
        super(List.of());
    }

    // TODO: add timestamps, probably use enum for type
    public synchronized void newMessage(String author, String discord, String content, boolean confirmed, String type) {
        newMessage(addDiscord(author, discord), content, confirmed, type);
    }
    public synchronized void newMessage(String author, String content, boolean confirmed, String type) {
        content = stripIllegal(content);
        if (!confirmed) unconfirmedIndex.add(messages.size());
        author = type + author;
        if (curDiscordChat != null) {
            if (confirmed && !unconfirmedIndex.isEmpty() && author.split("/")[0].equals(messages.get(unconfirmedIndex.getFirst())
                    .getLeft().split("/")[0]) && content.equals(messages.get(unconfirmedIndex.getFirst()).getRight())) {
                // confirming message
                if (curDiscordChat.getEntryCount() > unconfirmedIndex.getFirst())
                    curDiscordChat.getEntry(unconfirmedIndex.getFirst()).confirm();
                unconfirmedIndex.removeFirst();
            } else {
                // new message, potentially unconfirmed
                messages.add(new Pair<>(author, content));
                addDiscordMessage(curDiscordChat, author, content, confirmed);
            }
        } else {
            messages.add(new Pair<>(author, content));
        }
    }

    public void addAll(DiscordChatWidget body) {
        int unconfirmedI = 0;
        for (int i = 0; i < messages.size(); i++) {
            Pair<String, String> message = messages.get(i);
            if (unconfirmedI < unconfirmedIndex.size() && i == unconfirmedIndex.get(unconfirmedI)) {
                addDiscordMessage(body, parse(message.getLeft()), message.getRight(), false);
                ++unconfirmedI;
            } else
                addDiscordMessage(body, parse(message.getLeft()), message.getRight(), true);
        }
    }

    public void clearMessages() {
        messages.clear();
        unconfirmedIndex.clear();
    }

    private synchronized void addDiscordMessage(DiscordChatWidget body, String author, String content, boolean confirmed) {
        body.addMessage(parse(author.substring(2)), content, confirmed, author.startsWith(GUILD_MESSAGE));
    }

    public void setDiscordChat(DiscordChatWidget to) {
        curDiscordChat = to;
    }

    public String stripIllegal(String input) {
        return input.replaceAll("[\u200C\uE087\uE013\u2064\uE071\uE012\uE000\uE089\uE088\uE07F\uE08B\uE07E\uE080ÁÀ֎]", "");
    }

    /** still needs prepend and highlighting **/
    public Text toDiscordMessage(String author, String content) {
        return Text.empty().append(FontUtils.BannerPillFont.parseStringWithFill("discord")
                        .fillStyle(ColourUtils.LIGHT_PURPLE)).append(" ")
                .append(Text.literal(author)
                        .fillStyle(ColourUtils.LIGHT_PURPLE).append(": "))
                .append(Text.literal(content)
                        .setStyle(ColourUtils.LIGHT_PURPLE));
    }

    public String addDiscord(String str, String discord) {
        if (discord.isBlank() || discord.equals("@none")) return str + "/";
        return str + "/" + discord;
    }

    public String parse(String author) {
        // TODO implement config for how discord should be displayed here
        String[] parts = author.split("/");
        if (parts.length > 2) {
            GuildApi.LOGGER.warn("malformed author: {}", author);
            return author;
        }
        String username = parts[0];
        String discord = parts.length == 2 ? parts[1] : "";
        if (discord.equals("@me")) discord = "";
        return username + (discord.isBlank() ? "" : "/§o" + discord);
    }

    @Override
    public void init() {
    }
}
