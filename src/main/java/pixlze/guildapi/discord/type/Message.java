package pixlze.guildapi.discord.type;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import pixlze.guildapi.utils.ColourUtils;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.text.FontUtils;
import pixlze.guildapi.utils.text.TextUtils;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Message {
    private final static Pattern ONE_LINE_PATTERN = Pattern.compile("^\\*\\*(?<author>\\w+):\\*\\* (?<content>.*)$");
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
        if (replyAuthor != null) replyAuthor = replyAuthor.replaceAll("\\\\", "");
        if (replyContent != null) replyContent = replyContent.replaceAll("\\\\", "");
        if (replyContent != null) {
            if (replyAuthor == null || replyAuthor.equals("Discord Only")) {
                Matcher m = ONE_LINE_PATTERN.matcher(replyContent);
                if (m.find()) {
                    this.replyAuthor = m.group("author");
                    this.replyContent = m.group("content");
                } else {
                    this.replyAuthor = replyAuthor;
                    this.replyContent = replyContent;
                }
            } else {
                this.replyAuthor = replyAuthor;
                this.replyContent = replyContent;
            }
        } else {
            this.replyAuthor = replyAuthor;
            this.replyContent = null;
        }
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
            pre.append(getReply(ColourUtils.LIGHT_PURPLE));
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

    public Text getReply(Style override) {
        return getReply(override, override);
    }

    public Text getReply(Style override1, Style override2) {
        if (replyContent == null) return Text.empty();
        return Text.empty().append(Text.literal(" replying to ").fillStyle(override1))
                .append(Text.literal("this message")
                        .fillStyle(override2.withUnderline(true)
                                .withHoverEvent(new HoverEvent.ShowText(Text.empty()
                                        .append(Text.literal(replyAuthor + ": ")
                                                .fillStyle(Style.EMPTY.withBold(true)))
                                        .append(Text.literal(replyContent))))));
    }

    public Text getReplyAuthor() {
        if (replyAuthor == null) return Text.empty();
        return Text.literal(" replying to " + replyAuthor);
    }

    public String getContent() {
        return highlight.apply(this.content);
    }

    public List<MutableText> getContentLines(int maxWidth) {
        return TextUtils.wrapToMutableText(Text.literal(highlight.apply(content)), maxWidth);
    }
}
