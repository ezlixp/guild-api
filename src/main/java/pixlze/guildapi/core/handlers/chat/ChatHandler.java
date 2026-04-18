package pixlze.guildapi.core.handlers.chat;

import net.minecraft.text.Text;
import pixlze.guildapi.core.components.Handler;
import pixlze.guildapi.core.handlers.chat.event.ChatMessageReceived;
import pixlze.guildapi.mc.event.WynnChatMessage;

public final class ChatHandler extends Handler {
    @Override
    public void init() {
        WynnChatMessage.EVENT.register(this::onWynnMessage);
    }

    private void onWynnMessage(Text message) {
        postChatLine(message);
    }

    public void postChatLine(Text line) {
        ChatMessageReceived.EVENT.invoker().interact(line);
    }
}
