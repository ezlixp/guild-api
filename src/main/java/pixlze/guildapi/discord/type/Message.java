package pixlze.guildapi.discord.type;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.*;
import pixlze.guildapi.utils.ColourUtils;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.text.FontUtils;

import java.util.List;
import java.util.function.Function;

public class Message {
    private final String mcUsername;
    private final String discord;
    private final String content;
    private final boolean isGuild;
    private final TextRenderer textRenderer;
    private final Function<String, String> highlight;

    // if just mcusername and empty discord, don't do any special formatting as that is the case used for headers that
    // aren't username (like triangl info)
    public Message(String mcUsername, String discord, String content, boolean isGuild, Function<String, String> highlight) {
        this.mcUsername = mcUsername;
        this.discord = discord;
        this.content = content;
        this.isGuild = isGuild;
        this.textRenderer = McUtils.mc().textRenderer;
        this.highlight = highlight;
    }

    public boolean isGuild() {
        return this.isGuild;
    }

    public boolean equals(Message other) {
        return true;
    }

    public MutableText get() {
        return Text.empty().append(FontUtils.BannerPillFont.parseStringWithFill("discord")
                        .fillStyle(ColourUtils.LIGHT_PURPLE)).append(" ")
                .append(getAuthor()
                        .fillStyle(ColourUtils.LIGHT_PURPLE).append(": "))
                .append(Text.literal(highlight.apply(content))
                        .setStyle(ColourUtils.LIGHT_PURPLE));
    }

    public MutableText getAuthor() {
        if (discord.isBlank()) {
            return Text.literal(highlight.apply(mcUsername));
        }
        if (mcUsername.isBlank()) {
            return Text.literal("§o" + highlight.apply(discord));
        }
        return Text.literal(highlight.apply(mcUsername) + "/§o" + highlight.apply(discord)).setStyle(Style.EMPTY.withHoverEvent
                (new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(mcUsername + "'s discord username is " + discord))));
    }

    public String getContent() {
        return highlight.apply(this.content);
    }

    public List<OrderedText> getContentLines(int maxWidth) {
        return textRenderer.wrapLines(Text.literal(highlight.apply(content)), maxWidth);
    }
}
