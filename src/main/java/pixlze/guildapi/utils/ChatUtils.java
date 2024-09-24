package pixlze.guildapi.utils;

import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import pixlze.guildapi.GuildApi;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ChatUtils {
    private static StringBuilder currentVisit;
    public static final StringVisitable.StyledVisitor<String> PLAIN_VISITOR = new StringVisitable.StyledVisitor<>() {
        @Override
        public Optional<String> accept(Style style, String asString) {
            currentVisit.append(asString.replaceAll("§.", ""));
            return Optional.empty();
        }
    };
    private static boolean afterNewline;
    public static final StringVisitable.StyledVisitor<String> STYLED_VISITOR = (style, asString) -> {
        addStyleCodes(style, asString, "§", "\\\\n");
        return Optional.empty();
    };
    public static final StringVisitable.StyledVisitor<String> RAID_VISITOR = new StringVisitable.StyledVisitor<>() {
        @Override
        public Optional<String> accept(Style style, String asString) {
            if (!style.getFont().equals(Identifier.of("default"))) {
                afterNewline = true;
                return Optional.empty();
            }
            if (style.getHoverEvent() != null) {
                List<Text> onHover = null;
                if (style.getHoverEvent().getValue(style.getHoverEvent().getAction()) instanceof Text) {
                    onHover = ((Text) Objects.requireNonNull(
                            style.getHoverEvent().getValue(style.getHoverEvent().getAction()))).getSiblings();
                } else {
                    GuildApi.LOGGER.info("non text event: {} in message {}", style, asString);
                }
                try {
                    if (asString.indexOf('/') == -1) {
                        if (onHover != null) {
                            if (onHover.size() > 2 && onHover.get(1).getString() != null && Objects.requireNonNull(
                                    onHover.get(1).getString()).contains("nickname is")) {
                                if (!afterNewline) {
                                    currentVisit.append("§e");
                                }
                                currentVisit.append(onHover.getFirst().getString()).append("§b");
                            } else if (!onHover.isEmpty() && onHover.getFirst()
                                    .getString() != null && onHover.getFirst()
                                    .getString()
                                    .contains(
                                            "real username is")) {
                                if (onHover.size() > 1) {
                                    if (!afterNewline) {
                                        currentVisit.append("§e");
                                    }
                                    currentVisit.append(onHover.get(1).getString()).append("§b");
                                } else {
                                    if (!afterNewline) {
                                        currentVisit.append("§e");
                                    }
                                    currentVisit.append(onHover.getFirst().getSiblings().getFirst().getString())
                                            .append("§b");
                                }
                            } else {
                                currentVisit.append(asString.replaceAll("\\n", ""));
                            }
                        } else {
                            currentVisit.append(asString.replaceAll("\\n", ""));
                        }
                    } else if (onHover == null || onHover.size() < 2 || onHover.get(1)
                            .getString() == null || !Objects.requireNonNull(
                            onHover.get(1).getString()).contains("'s nickname is ")) {
                        currentVisit.append(asString.replaceAll("\\n", ""));
                    }
                } catch (Exception e) {
                    GuildApi.LOGGER.error("raid visitor hover error: {} {} {} with astring {}", e.getMessage(), e,
                            asString, onHover);
                }
            } else {
                addStyleCodes(style, asString, "§", "");
            }
            afterNewline = false;
            return Optional.empty();
        }
    };

    private static void addStyleCodes(Style style, String asString, String formatCode, String newline) {
        if (!afterNewline) {
            if (style.getColor() != null) {
                int colorIndex = 0;
                for (Formatting format : Formatting.values()) {
                    if (format.getColorValue() != null && format.getColorValue().equals(style.getColor().getRgb())) {
                        colorIndex = format.getColorIndex();
                        break;
                    }
                }
                currentVisit.append(formatCode)
                        .append(Objects.requireNonNull(Formatting.byColorIndex(colorIndex)).getCode());
            }
            if (style.isBold()) {
                currentVisit.append(formatCode).append(Formatting.BOLD.getCode());
            }
            if (style.isItalic()) {
                currentVisit.append(formatCode).append(Formatting.ITALIC.getCode());
            }
            if (style.isUnderlined()) {
                currentVisit.append(formatCode).append(Formatting.UNDERLINE.getCode());
            }
            if (style.isStrikethrough()) {
                currentVisit.append(formatCode).append(Formatting.STRIKETHROUGH.getCode());
            }
            if (style.isObfuscated()) {
                currentVisit.append(formatCode).append(Formatting.OBFUSCATED.getCode());
            }
        }
        currentVisit.append(asString.replaceAll("\\n", newline).replaceAll("§", formatCode));
    }

    public static String parseRaid(Text text) {
        currentVisit = new StringBuilder();
        text.visit(RAID_VISITOR, text.getStyle());
        return currentVisit.toString();
    }

    public static String parsePlain(Text text) {
        currentVisit = new StringBuilder();
        text.visit(PLAIN_VISITOR, text.getStyle());
        return currentVisit.toString();
    }

    public static String parseStyled(Text text) {
        currentVisit = new StringBuilder();
        text.visit(STYLED_VISITOR, text.getStyle());
        return currentVisit.toString();
    }
}
