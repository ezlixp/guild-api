package pixlze.guildapi.discord;

import pixlze.guildapi.core.components.Manager;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.discord.type.Message;
import pixlze.guildapi.features.discord.DiscordBridgeFeature;
import pixlze.guildapi.screens.discord.widgets.DiscordChatWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DiscordMessageManager extends Manager {
    private final List<Message> messages = new ArrayList<>();
    private final ArrayList<Integer> unconfirmedIndex = new ArrayList<>();
    private DiscordChatWidget curDiscordChat;

    public DiscordMessageManager() {
        super(List.of());
    }

    public void newMessage(String header, String content, String replyAuthor, String replyContent, boolean isGuild, boolean confirmed) {
        newMessage(header, "", content, replyAuthor, replyContent, isGuild, confirmed);
    }

    public synchronized void newMessage(String mcUsername, String discord, String content, String replyAuthor, String replyContent, boolean isGuild, boolean confirmed) {
        content = stripIllegal(content);
        if (!confirmed) unconfirmedIndex.add(messages.size());
        Function<String, String> highlight = ((DiscordBridgeFeature) Managers.Feature.getFeatureInstance(DiscordBridgeFeature.class))::highlightMessage;
        Message message = new Message(mcUsername, discord, content, replyAuthor, replyContent, isGuild, highlight);
        if (curDiscordChat != null) {
            if (confirmed && !unconfirmedIndex.isEmpty() && message.equals(messages.get(unconfirmedIndex.getFirst()))) {
                // confirming message
                if (curDiscordChat.getEntryCount() > unconfirmedIndex.getFirst())
                    curDiscordChat.getEntry(unconfirmedIndex.getFirst()).confirm();
                unconfirmedIndex.removeFirst();
            } else {
                // new message, potentially unconfirmed
                messages.add(message);
                addDiscordMessage(curDiscordChat, message, confirmed);
            }
        } else {
            messages.add(new Message(mcUsername, discord, content, replyAuthor, replyContent, isGuild, highlight));
        }
    }

    public void addAll(DiscordChatWidget body) {
        int unconfirmedI = 0;
        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            if (unconfirmedI < unconfirmedIndex.size() && i == unconfirmedIndex.get(unconfirmedI)) {
                addDiscordMessage(body, message, false);
                ++unconfirmedI;
            } else
                addDiscordMessage(body, message, true);
        }
    }

    public void clearMessages() {
        messages.clear();
        unconfirmedIndex.clear();
    }

    private synchronized void addDiscordMessage(DiscordChatWidget body, Message message, boolean confirmed) {
        body.addMessage(message, confirmed, message.isGuild());
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
