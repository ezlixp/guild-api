package pixlze.guildapi.discord;

import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import pixlze.guildapi.core.components.Manager;
import pixlze.guildapi.screens.discord.widgets.DiscordChatWidget;

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

    // TODO: add timestamps, and add unconfirmed messages (greyed out)
    public synchronized void newMessage(String author, String content, boolean confirmed, String type) {
        content = stripIllegal(content);
        if (!confirmed) unconfirmedIndex.add(messages.size());
        author = type + author;
        if (curDiscordChat != null) {
            if (confirmed && !unconfirmedIndex.isEmpty() && author.equals(messages.get(unconfirmedIndex.getFirst())
                    .getLeft()) && content.equals(messages.get(unconfirmedIndex.getFirst()).getRight())) {
                if (curDiscordChat.getEntryCount() > unconfirmedIndex.getFirst())
                    curDiscordChat.getEntry(unconfirmedIndex.getFirst()).confirm();
                unconfirmedIndex.removeFirst();
            } else {
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
                addDiscordMessage(body, message.getLeft(), message.getRight(), false);
                ++unconfirmedI;
            } else
                addDiscordMessage(body, message.getLeft(), message.getRight(), true);
        }
    }

    public void clearMessages() {
        messages.clear();
        unconfirmedIndex.clear();
    }

    private synchronized void addDiscordMessage(DiscordChatWidget body, String author, String content, boolean confirmed) {
        body.addMessage(Text.of(author.substring(2)), Text.of(content), confirmed, author.startsWith(GUILD_MESSAGE));
    }

    public void setDiscordChat(DiscordChatWidget to) {
        curDiscordChat = to;
    }

    public String stripIllegal(String input) {
        return input.replaceAll("[\u200C\uE087\uE013\u2064\uE071\uE012\uE000\uE089\uE088\uE07F\uE08B\uE07E\uE080ÁÀ֎]", "");
    }

    @Override
    public void init() {
    }
}
