package pixlze.guildapi.handlers.chat;

import net.minecraft.text.Text;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.mc.event.WynnChatMessageEvents;

public final class ChatHandler {
    private void onWynnMessage(Text message) {
        GuildApi.LOGGER.info("message");
    }

    public void init() {
        WynnChatMessageEvents.CHAT.register(this::onWynnMessage);
    }
}
