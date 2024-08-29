package pixlze.utils;

import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

import java.util.Objects;
import java.util.Optional;

public class Visitors {
    public static String currentVisit;
    public static final StringVisitable.Visitor<String> PLAIN_VISITOR = new StringVisitable.Visitor<>() {
        @Override
        public Optional<String> accept(String asString) {
            currentVisit += asString;
            return Optional.empty();
        }
    };
    public static final StringVisitable.StyledVisitor<String> STYLED_VISITOR = new StringVisitable.StyledVisitor<>() {
        @Override
        public Optional<String> accept(Style style, String asString) {
            if (style.getFont().getPath().startsWith("hud")) {
                return "break".describeConstable();
            }
            if (style.getColor() != null) {
                int colorIndex = 0;
                for (Formatting format : Formatting.values()) {
                    if (format.getColorValue() != null && format.getColorValue().equals(style.getColor().getRgb())) {
                        colorIndex = format.getColorIndex();
                        break;
                    }
                }
                currentVisit += "&" + Objects.requireNonNull(Formatting.byColorIndex(colorIndex)).getCode();
            }
            if (style.isBold()) {
                currentVisit += "&" + Formatting.BOLD.getCode();
            }
            if (style.isItalic()) {
                currentVisit += "&" + Formatting.ITALIC.getCode();
            }
            if (style.isUnderlined()) {
                currentVisit += "&" + Formatting.UNDERLINE.getCode();
            }
            if (style.isStrikethrough()) {
                currentVisit += "&" + Formatting.STRIKETHROUGH.getCode();
            }
            if (style.isObfuscated()) {
                currentVisit += "&" + Formatting.OBFUSCATED.getCode();
            }
            currentVisit += asString.replaceAll("\\n", "\\\\n");
            return Optional.empty();
        }
    };
}
