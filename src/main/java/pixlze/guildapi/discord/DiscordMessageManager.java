package pixlze.guildapi.discord;

import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import pixlze.guildapi.core.components.Manager;
import pixlze.guildapi.screens.discord.DiscordScreen;
import pixlze.guildapi.screens.discord.widgets.DiscordChatWidget;

import java.util.ArrayList;
import java.util.List;

public class DiscordMessageManager extends Manager {
    private final List<Pair<String, String>> messages = new ArrayList<>();
    private DiscordScreen curDiscordScreen;

    public DiscordMessageManager() {
        super(List.of());
    }

    public void newMessage(String author, String content) {
        messages.add(new Pair<>(author, content));
        if (curDiscordScreen != null && curDiscordScreen.body != null)
            addDiscordMessage(curDiscordScreen.body, author, content);
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

    public void setDiscordScreen(DiscordScreen to) {
        curDiscordScreen = to;
    }

    @Override
    public void init() {

    }
}
