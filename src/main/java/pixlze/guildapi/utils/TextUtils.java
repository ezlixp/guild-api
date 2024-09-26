package pixlze.guildapi.utils;

import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import pixlze.guildapi.GuildApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TextUtils {
    private static ArrayList<String> extractedUsernames;
    public static final StringVisitable.StyledVisitor<String> USERNAME_EXTRACTOR_VISITOR = (style, asString) -> {
        if (style.getHoverEvent() != null) {
            List<Text> onHover = null;
            if (style.getHoverEvent().getValue(style.getHoverEvent().getAction()) instanceof Text) {
                onHover = ((Text) Objects.requireNonNull(
                        style.getHoverEvent().getValue(style.getHoverEvent().getAction()))).getSiblings();
            } else {
                GuildApi.LOGGER.info("non text event while extracting: {} in message {}", style, asString);
            }
            if (asString.indexOf('/') == -1) {
                if (onHover != null) {
                    if (onHover.size() > 2 && onHover.get(1).getString() != null && Objects.requireNonNull(
                            onHover.get(1).getString()).contains("nickname is")) {
                        GuildApi.LOGGER.info("wynntils username found: {}", onHover.getFirst().getString());
                        extractedUsernames.add(onHover.getFirst().getString());
                    } else if (!onHover.isEmpty() && onHover.getFirst()
                            .getString() != null && onHover.getFirst()
                            .getString()
                            .contains(
                                    "real username is")) {
                        if (onHover.size() > 1) {
                            GuildApi.LOGGER.info("username found multi part: {}", onHover.get(1).getString());
                            extractedUsernames.add(onHover.get(1).getString());
                        } else {
                            GuildApi.LOGGER.info("username found sibling: {}", onHover.getFirst().getSiblings()
                                    .getFirst().getString());
                            extractedUsernames.add(onHover.getFirst().getSiblings().getFirst().getString());
                        }
                    }
                }
            }
        }
        return Optional.empty();
    };
    private static StringBuilder currentVisit;
    public static final StringVisitable.StyledVisitor<String> PLAIN_VISITOR = (style, asString) -> {
        currentVisit.append(asString.replaceAll("§.", ""));
        return Optional.empty();
    };
    private static boolean afterNewline;
    private static String formatCode = "§";
    public static final StringVisitable.StyledVisitor<String> STYLED_VISITOR = (style, asString) -> {
        addStyleCodes(style, asString, "\n");
        return Optional.empty();
    };
    public static final StringVisitable.StyledVisitor<String> RAID_VISITOR = (style, asString) -> {
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
                                currentVisit.append(formatCode).append("e");
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
            addStyleCodes(style, asString, "");
        }
        afterNewline = false;
        return Optional.empty();
    };

    private static void addStyleCodes(Style style, String asString, String newline) {
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

    public static List<Text> splitLines(Text message) {
        ArrayList<Text> splitted = new ArrayList<>();
        MutableText currentPart = Text.empty();
        for (Text part : message.getWithStyle(message.getStyle())) {
            if (part.getString().equals("\n")) {
                splitted.add(currentPart);
                currentPart = Text.empty();
            } else {
                currentPart.append(part);
            }
        }
        if (!currentPart.equals(Text.empty())) splitted.add(currentPart);
        return splitted;
    }

    public static List<String> extractUsernames(Text message) {
        extractedUsernames = new ArrayList<>();
        message.visit(USERNAME_EXTRACTOR_VISITOR, message.getStyle());
        return extractedUsernames;
    }

    public static String parseStyled(Text text, String formatCode) {
        TextUtils.formatCode = formatCode;
        currentVisit = new StringBuilder();
        text.visit(STYLED_VISITOR, text.getStyle());
        return currentVisit.toString();
    }

    public static String parseRaid(Text text, String formatCode) {
        TextUtils.formatCode = formatCode;
        currentVisit = new StringBuilder();
        text.visit(RAID_VISITOR, text.getStyle());
        return currentVisit.toString();
    }

    public static String parsePlain(Text text) {
        currentVisit = new StringBuilder();
        text.visit(PLAIN_VISITOR, text.getStyle());
        return currentVisit.toString();
    }
}
