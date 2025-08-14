package pixlze.guildapi.utils.type;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import pixlze.guildapi.utils.text.TextUtils;
import pixlze.guildapi.utils.text.type.TextParseOptions;

public enum Prepend {
    DEFAULT(Text.literal("[Guild API] ").setStyle(Style.EMPTY.withColor(Formatting.GOLD))),
    EMPTY(Text.empty()),
    GUILD(Text.literal("󏿼󏿿󏿾")
            .append(" ").setStyle(Style.EMPTY.withFont(Identifier.of("chat/prefix")).withColor(Formatting.AQUA)), Text.literal("\uDAFF\uDFFC\uE001\uDB00\uDC06")
            .append(" ").setStyle(Style.EMPTY.withFont(Identifier.of("chat/prefix")).withColor(Formatting.AQUA)));

    public static String lastBadge = "";
    public static int linesSinceBadge = 0;
    private final MutableText prepend;
    private final MutableText blockIndicator;

    Prepend(MutableText prepend) {
        this.prepend = prepend;
        this.blockIndicator = prepend;
    }

    Prepend(MutableText prepend, MutableText blockIndicator) {
        this.prepend = prepend;
        this.blockIndicator = blockIndicator;
    }

    public MutableText get() {
        if (lastBadge.equals(TextUtils.parseStyled(prepend, TextParseOptions.DEFAULT)) && linesSinceBadge < 18)
            return blockIndicator.copy();
        else {
            lastBadge = TextUtils.parseStyled(prepend, TextParseOptions.DEFAULT);
            linesSinceBadge = 0;
            return prepend.copy();
        }
    }

    public MutableText getWithStyle(Style style) {
        MutableText prependWithStyle = prepend.copy().fillStyle(style);
        if (lastBadge.equals(TextUtils.parseStyled(prependWithStyle, TextParseOptions.DEFAULT)) && linesSinceBadge < 18)
            return blockIndicator.copy().fillStyle(style);
        else {
            lastBadge = TextUtils.parseStyled(prependWithStyle, TextParseOptions.DEFAULT);
            linesSinceBadge = 0;
            return prependWithStyle;
        }
    }
}
