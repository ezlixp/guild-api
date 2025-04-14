package pixlze.guildapi.discord;

import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import pixlze.guildapi.core.components.Manager;
import pixlze.guildapi.screens.discord.DiscordChatScreen;
import pixlze.guildapi.screens.discord.widgets.DiscordChatWidget;

import java.util.ArrayList;
import java.util.List;

public class DiscordMessageManager extends Manager {
    private final List<Pair<String, String>> messages = new ArrayList<>();
    private DiscordChatScreen curDiscordChatScreen;

    public DiscordMessageManager() {
        super(List.of());
    }

    // TODO: add timestamps
    public void newMessage(String author, String content) {
        messages.add(new Pair<>(author, content));
        if (curDiscordChatScreen != null && curDiscordChatScreen.body != null)
            addDiscordMessage(curDiscordChatScreen.body, author, content);
    }

    public void addAll(DiscordChatWidget body) {
        for (Pair<String, String> message : messages) {
            addDiscordMessage(body, message.getLeft(), message.getRight());
        }
    }

    public void clearMessages() {
        messages.clear();
    }

    private void addDiscordMessage(DiscordChatWidget body, String author, String content) {
        body.addMessage(Text.of(author), Text.of(content));
    }

    public void setDiscordScreen(DiscordChatScreen to) {
        curDiscordChatScreen = to;
    }

    @Override
    public void init() {

    }
}
