package pixlze.guildapi.discord.type;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import pixlze.guildapi.utils.ColourUtils;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.text.FontUtils;
import pixlze.guildapi.utils.text.TextUtils;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

public class Message {
    private final static Pattern ONE_LINE_PATTERN = Pattern.compile("");
    private final String mcUsername;
    private final String discord;
    private final String content;
    private final String replyAuthor;
    private final String replyContent;
    private final boolean isGuild;
    private final TextRenderer textRenderer;
    private final Function<String, String> highlight;

    // if just mcusername and empty discord, don't do any special formatting as that is the case used for headers that
    // aren't username (like triangl info)
    public Message(String mcUsername, String discord, String content, String replyAuthor, String replyContent, boolean isGuild, Function<String, String> highlight) {
        this.mcUsername = mcUsername == null ? "":mcUsername;
        this.discord = discord == null ? "":discord;
        this.content = content == null ? "":content;
        this.replyAuthor = replyAuthor;
        this.replyContent = replyContent;
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
        MutableText pre = Text.empty()
                .append(FontUtils.BannerPillFont.parseStringWithFill("discord").fillStyle(ColourUtils.LIGHT_PURPLE))
                .append(" ")
                .append(getAuthor().fillStyle(ColourUtils.LIGHT_PURPLE));
        if (replyContent != null) {
            pre.append(Text.literal(" replying to ").fillStyle(ColourUtils.LIGHT_PURPLE))
                    .append(Text.literal("this message")
                            .fillStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE).withUnderline(true)
                                    .withHoverEvent(new HoverEvent.ShowText(Text.empty()
                                            .append(Text.literal(replyAuthor + ": ")
                                                    .fillStyle(Style.EMPTY.withBold(true)))
                                            .append(Text.literal(replyContent))))));
        }
        return pre.append(": ").append(Text.literal(highlight.apply(content))
                .setStyle(ColourUtils.LIGHT_PURPLE));
    }

    public MutableText getAuthor() {
        if (discord.isBlank()) {
            return Text.literal(highlight.apply(mcUsername));
        }
        if (mcUsername.isBlank()) {
            return Text.literal("§o" + highlight.apply(discord));
        }
        return Text.literal(highlight.apply(mcUsername) + "/§o" + highlight.apply(discord))
                .setStyle(Style.EMPTY.withHoverEvent
                        (new HoverEvent.ShowText(Text.literal(mcUsername + "'s discord name is " + discord))));
    }

    public String getContent() {
        return highlight.apply(this.content);
    }

    public List<MutableText> getContentLines(int maxWidth) {
        return TextUtils.wrapToMutableText(Text.literal(highlight.apply(content)), maxWidth);
    }
}
