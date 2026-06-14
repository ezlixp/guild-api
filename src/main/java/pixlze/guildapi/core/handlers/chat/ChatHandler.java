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
    private final Text raidmessagenol31split = Text.empty().setStyle(ColourUtils.AQUA)
            .append(Text.literal("\uDAFF\uDFFC\uE006\uDAFF\uDFFF\uE002\uDAFF\uDFFE"))
            .append(Text.literal(" "))
            .append(Text.literal("Pickelated").setStyle(ColourUtils.YELLOW))
            .append(Text.literal(", "))
            .append(Text.literal("vex710").setStyle(ColourUtils.YELLOW))
            .append(Text.literal(", and "))
            .append(Text.literal("Essentuan").setStyle(ColourUtils.YELLOW))
            .append(Text.literal(" finished "))
            .append(Text.literal("Orphion's").setStyle(ColourUtils.DARK_AQUA).append(Text.literal("\n")
                            .append(Text.empty().setStyle(ColourUtils.AQUA.withBold(false).withUnderline(false))
                                    .append(Text.literal("\uDAFF\uDFFC\uE001\uDB00\uDC06")).append(Text.literal(" "))))
                    .append(Text.literal("Nexus of Light")))
            .append(Text.literal(" and claimed "))
            .append(Text.literal("2048x Emeralds").setStyle(ColourUtils.DARK_AQUA))
            .append(Text.literal(", "))
            .append(Text.literal("2x Aspects").setStyle(ColourUtils.DARK_AQUA))
            .append(Text.literal(", "))
            .append(Text.empty().setStyle(ColourUtils.DARK_AQUA).append(
                    Text.literal("\n")
                            .append(Text.empty().setStyle(ColourUtils.AQUA.withBold(false).withUnderline(false))
                                    .append(Text.literal("\uDAFF\uDFFC\uE001\uDB00\uDC06")).append(Text.literal(" ")))
            ).append(Text.literal("+312m Guild Experience")))
            .append(Text.literal(", and "))
            .append(Text.literal("+330 Seasonal Rating").setStyle(ColourUtils.DARK_AQUA));

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
