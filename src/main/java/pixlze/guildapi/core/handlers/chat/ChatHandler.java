package pixlze.guildapi.core.handlers.chat;

import net.minecraft.text.Text;
import pixlze.guildapi.core.components.Handler;
import pixlze.guildapi.core.handlers.chat.event.ChatMessageReceived;
import pixlze.guildapi.mc.event.WynnChatMessage;
import pixlze.guildapi.utils.ColourUtils;

public final class ChatHandler extends Handler {
    private final Text emeraldMessageEasy = Text.empty().append(Text.literal("\uDAFF\uDFFC\uE001\uDB00\uDC06"))
            .append(Text.literal(" ")).append(Text.literal("Chunkywaterx rewarded ").setStyle(ColourUtils.DARK_AQUA)
                    .append(Text.literal("1024 Emeralds").setStyle(ColourUtils.YELLOW)).append(Text.literal(" to "))
                    .append(Text.literal("Doggc").setStyle(ColourUtils.DARK_AQUA)));

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
