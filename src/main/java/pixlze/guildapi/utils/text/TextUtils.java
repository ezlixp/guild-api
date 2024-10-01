package pixlze.guildapi.utils.text;

import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.utils.text.type.TextParseOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public class TextUtils {
    private static final Pattern BLOCK_MARKER_PATTERN = Pattern.compile("^(§.)?\uDAFF\uDFFC\uE001\uDB00\uDC06(§.)?$");

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


    public static String parseStyled(Text text, TextParseOptions options) {
        TextVisitors.first = true;
        TextVisitors.options = options;
        TextVisitors.currentVisit = new StringBuilder();
        text.visit(TextVisitors.STYLED_VISITOR, text.getStyle());
        return TextVisitors.currentVisit.toString();
    }

    public static String parseRaid(Text text, TextParseOptions options) {
        TextVisitors.options = options;
        TextVisitors.currentVisit = new StringBuilder();
        text.visit(TextVisitors.RAID_VISITOR, text.getStyle());
        return TextVisitors.currentVisit.toString();
    }

    public static String parsePlain(Text text) {
        TextVisitors.currentVisit = new StringBuilder();
        text.visit(TextVisitors.PLAIN_VISITOR, text.getStyle());
        return TextVisitors.currentVisit.toString();
    }
    static class TextVisitors {
        static StringBuilder currentVisit;
        static boolean first = false;
        static boolean afterBlockMarker;
        static TextParseOptions options;

        public static final StringVisitable.StyledVisitor<String> PLAIN_VISITOR = (style, asString) -> {
            currentVisit.append(asString.replaceAll("§.", ""));
            return Optional.empty();
        };
        public static final StringVisitable.StyledVisitor<String> STYLED_VISITOR = (style, asString) -> {
            if (options.extractUsernames && style.getHoverEvent() != null) {
                handleStylesWithHover(style, asString);
            } else {
                handleStyles(style, asString);
            }
            handleStyles(style, asString);
            return Optional.empty();
        };
        public static final StringVisitable.StyledVisitor<String> RAID_VISITOR = (style, asString) -> {
            if (!style.getFont().equals(Identifier.of("default"))) {
                afterBlockMarker = true;
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
                                if (!afterBlockMarker) {
                                    currentVisit.append(options.formatCode).append("e");
                                }
                                currentVisit.append(onHover.getFirst().getString()).append("§b");
                            } else if (!onHover.isEmpty() && onHover.getFirst()
                                    .getString() != null && onHover.getFirst()
                                    .getString()
                                    .contains(
                                            "real username is")) {
                                if (onHover.size() > 1) {
                                    if (!afterBlockMarker) {
                                        currentVisit.append("§e");
                                    }
                                    currentVisit.append(onHover.get(1).getString()).append("§b");
                                } else {
                                    if (!afterBlockMarker) {
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
                handleStyles(style, asString);
            }
            afterBlockMarker = false;
            return Optional.empty();
        };

        private static void handleStyles(Style style, String asString) {
            if (BLOCK_MARKER_PATTERN.matcher(asString).find() && !first) {
                afterBlockMarker = true;
                return;
            }
            if (!afterBlockMarker) {
                if (style.getColor() != null) {
                    int colorIndex = 0;
                    for (Formatting format : Formatting.values()) {
                        if (format.getColorValue() != null && format.getColorValue().equals(style.getColor().getRgb())) {
                            colorIndex = format.getColorIndex();
                            break;
                        }
                    }
                    TextVisitors.currentVisit.append(options.formatCode)
                            .append(Objects.requireNonNull(Formatting.byColorIndex(colorIndex)).getCode());
                }
                if (style.isBold()) {
                    TextVisitors.currentVisit.append(options.formatCode).append(Formatting.BOLD.getCode());
                }
                if (style.isItalic()) {
                    TextVisitors.currentVisit.append(options.formatCode).append(Formatting.ITALIC.getCode());
                }
                if (style.isUnderlined()) {
                    TextVisitors.currentVisit.append(options.formatCode).append(Formatting.UNDERLINE.getCode());
                }
                if (style.isStrikethrough()) {
                    TextVisitors.currentVisit.append(options.formatCode).append(Formatting.STRIKETHROUGH.getCode());
                }
                if (style.isObfuscated()) {
                    TextVisitors.currentVisit.append(options.formatCode).append(Formatting.OBFUSCATED.getCode());
                }
            } else {
                afterBlockMarker = false;
                asString = asString.substring(1);
            }
            TextVisitors.currentVisit.append(asString.replaceAll("\\n", options.newline).replaceAll("§", options.formatCode));
            if (first) first = false;
        }
        private static void handleStylesWithHover(Style style, String asString) {
            assert style.getHoverEvent() != null;
                if (style.getHoverEvent().getValue(style.getHoverEvent().getAction()) instanceof Text) {
                    List<Text> onHover = ((Text) Objects.requireNonNull(
                            style.getHoverEvent().getValue(style.getHoverEvent().getAction()))).getSiblings();
                    if (asString.indexOf('/') == -1) {
                        if (onHover != null) {
                            if (onHover.size() > 2 && onHover.get(1).getString() != null && Objects.requireNonNull(
                                    onHover.get(1).getString()).contains("nickname is")) {
                                GuildApi.LOGGER.info("wynntils username found: {} {}", style, asString);
                                currentVisit.append(onHover.getFirst().getString());
                            } else if (!onHover.isEmpty() && onHover.getFirst().getString() != null && onHover.getFirst().getString().contains("real username is")) {
                                if (onHover.size() > 1) {
                                    GuildApi.LOGGER.info("username found multi part: {} {}", style, asString);
                                    currentVisit.append(onHover.get(1).getString());
                                } else {
                                    GuildApi.LOGGER.info("username found sibling: {} {}", style, asString);
                                    currentVisit.append(onHover.getFirst().getSiblings().getFirst().getString());
                                }
                            }
                        }
                    }
                }
        }
    }
}
