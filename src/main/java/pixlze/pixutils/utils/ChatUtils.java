package pixlze.pixutils.utils;

import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import pixlze.pixutils.core.PixUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ChatUtils {
    public static StringBuilder currentVisit;
    public static final StringVisitable.StyledVisitor<String> RAID_VISITOR = new StringVisitable.StyledVisitor<>() {
        @Override
        public Optional<String> accept(Style style, String asString) {
            if (style.getFont().getPath().startsWith("hud")) {
                return "break".describeConstable();
            }
            if (style.getHoverEvent() != null) {
                List<Text> onHover = null;
                if (style.getHoverEvent().getValue(style.getHoverEvent().getAction()) instanceof Text) {
                    onHover = ((Text) Objects.requireNonNull(style.getHoverEvent().getValue(style.getHoverEvent().getAction()))).getSiblings();
                } else {
                    PixUtils.LOGGER.info("non text event: {} in message {}", style, asString);
                }
                try {
                    if (asString.indexOf('/') == -1) {
                        if (onHover != null) {
                            if (onHover.size() > 2 && onHover.get(1).getString() != null && Objects.requireNonNull(onHover.get(1).getString()).contains("nickname is"))
                                currentVisit.append("&e").append(onHover.getFirst().getString()).append("&b");
                            else if (!onHover.isEmpty() && onHover.getFirst().getString() != null && onHover.getFirst().getString().contains("real username is")) {
                                PixUtils.LOGGER.info("adding {}", onHover);
                                if (onHover.size() > 1) {
                                    currentVisit.append("&e").append(onHover.get(1).getString()).append("&b");
                                } else {
                                    currentVisit.append("&e").append(onHover.getFirst().getSiblings().getFirst().getString()).append("&b");
                                }
                            } else {
                                currentVisit.append(asString.replaceAll("\\n", "").replaceAll("§", "&"));
                            }
                        } else {
                            currentVisit.append(asString.replaceAll("\\n", "").replaceAll("§", "&"));
                        }
                    } else if (onHover == null || onHover.size() < 2 || onHover.get(1).getString() == null || !Objects.requireNonNull(onHover.get(1).getString()).contains("'s nickname is ")) {
                        currentVisit.append(asString.replaceAll("\\n", "").replaceAll("§", "&"));
                    }
                } catch (Exception e) {
                    PixUtils.LOGGER.error("raid visitor hover error: {} {} {} with astring {}", e.getMessage(), e, asString, onHover);
                }
            } else {
                if (style.getColor() != null) {
                    int colorIndex = 0;
                    for (Formatting format : Formatting.values()) {
                        if (format.getColorValue() != null && format.getColorValue().equals(style.getColor().getRgb())) {
                            colorIndex = format.getColorIndex();
                            break;
                        }
                    }
                    currentVisit.append("&").append(Objects.requireNonNull(Formatting.byColorIndex(colorIndex)).getCode());
                }
                if (style.isBold()) {
                    currentVisit.append("&").append(Formatting.BOLD.getCode());
                }
                if (style.isItalic()) {
                    currentVisit.append("&").append(Formatting.ITALIC.getCode());
                }
                if (style.isUnderlined()) {
                    currentVisit.append("&").append(Formatting.UNDERLINE.getCode());
                }
                if (style.isStrikethrough()) {
                    currentVisit.append("&").append(Formatting.STRIKETHROUGH.getCode());
                }
                if (style.isObfuscated()) {
                    currentVisit.append("&").append(Formatting.OBFUSCATED.getCode());
                }
                currentVisit.append(asString.replaceAll("\\n", "").replaceAll("§", "&"));
            }
            return Optional.empty();
        }
    };
    public static final StringVisitable.StyledVisitor<String> PLAIN_VISITOR = new StringVisitable.StyledVisitor<>() {
        @Override
        public Optional<String> accept(Style style, String asString) {
            if (style.getFont().equals(Identifier.of("default"))) {
                currentVisit.append(asString.replaceAll("§.", ""));
            }
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
                currentVisit.append("&").append(Objects.requireNonNull(Formatting.byColorIndex(colorIndex)).getCode());
            }
            if (style.isBold()) {
                currentVisit.append("&").append(Formatting.BOLD.getCode());
            }
            if (style.isItalic()) {
                currentVisit.append("&").append(Formatting.ITALIC.getCode());
            }
            if (style.isUnderlined()) {
                currentVisit.append("&").append(Formatting.UNDERLINE.getCode());
            }
            if (style.isStrikethrough()) {
                currentVisit.append("&").append(Formatting.STRIKETHROUGH.getCode());
            }
            if (style.isObfuscated()) {
                currentVisit.append("&").append(Formatting.OBFUSCATED.getCode());
            }
            currentVisit.append(asString.replaceAll("\\n", "\\\\n").replaceAll("§", "&"));
            return Optional.empty();
        }
    };
}
