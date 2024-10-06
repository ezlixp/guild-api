package pixlze.guildapi.utils.type;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public enum Prepend {
    DEFAULT(Text.empty().append(Text.literal("[Guild API] ").setStyle(Style.EMPTY.withColor(Formatting.GOLD)))),
    EMPTY(Text.empty()),
    GUILD(Text.empty().append(Text.literal("󏿼󏿿󏿾")
            .append(" ").setStyle(Style.EMPTY.withFont(Identifier.of("chat")).withColor(Formatting.AQUA))));

    private final MutableText prepend;

    Prepend(MutableText prepend) {
        this.prepend = prepend;
    }

    public MutableText get() {
        return prepend.copy();
    }

    public MutableText getWithStyle(Style style) {
        return prepend.copy().fillStyle(style);
    }
}
