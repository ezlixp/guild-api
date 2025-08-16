package pixlze.guildapi.utils.text;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import pixlze.guildapi.mc.mixin.accessors.ChatHudAccessorInvoker;
import pixlze.guildapi.utils.McUtils;
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
        // If all content isn't in siblings, we can just keep it as it is.
        if (!message.getContent().toString().equals("empty")) {
            splitted.add(message);
            return splitted;
        }
        MutableText currentPart = Text.empty();
        // Use getsiblings isntead of message.getwithstyle since message.getwithstyle flattens
        // all nested siblings. Chat screens are defined by \n's in top level and after the wardrobe update
        // \n's appear in nested siblings which is an issue when they are flattened
        for (Text part : message.getSiblings()) {
            if (part.getString().isEmpty() || part.getString().equals("\n")) {
                if (splitted.size() < 2) splitted.add(currentPart);
            } else {
                currentPart.append(part);
            }
        }
        if (!currentPart.getString().isEmpty() || splitted.size() < 2) splitted.add(currentPart);
        return splitted;
    }

    public static String wrapText(String text, int maxWidth) {
        MinecraftClient client = McUtils.mc();
        if (client == null || client.textRenderer == null) return text;

        StringBuilder wrapped = new StringBuilder();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            if (client.textRenderer.getWidth(line + word) > maxWidth) {
                wrapped.append(line).append("\n");
                line = new StringBuilder();
            }
            line.append(word).append(" ");
        }
        wrapped.append(line); // Add last line

        return wrapped.toString();
    }


    public static String parseStyled(StringVisitable text, TextParseOptions options) {
        TextVisitors.first = true;
        TextVisitors.options = options;
        TextVisitors.currentVisit = new StringBuilder();
        text.visit(TextVisitors.STYLED_VISITOR, Style.EMPTY);
        return TextVisitors.currentVisit.toString();
    }

    public static String parsePlain(StringVisitable text) {
        TextVisitors.currentVisit = new StringBuilder();
        text.visit(TextVisitors.PLAIN_VISITOR, Style.EMPTY);
        return TextVisitors.currentVisit.toString();
    }

    public static Text stringVisitableToText(StringVisitable visitable) {
        MutableText out = Text.empty();
        visitable.visit((style, asString) -> {
            out.append(Text.literal(asString).setStyle(style));
            return Optional.empty();
        }, Style.EMPTY);
        return out;
    }

    public static boolean isFormatting(String text, int index) {
        if (index + 1 >= text.length() || index < 0) return false;
        return text.charAt(index) == '§' && Formatting.byCode(text.charAt(index + 1)) != null;
    }

    public static Text toBlockMessage(Text text, Style prependStyle) {
        MinecraftClient client = MinecraftClient.getInstance();
        ChatHud chatHud = client.inGameHud.getChatHud();
        ChatHudAccessorInvoker chatHudAccessorInvoker = (ChatHudAccessorInvoker) chatHud;
        TextHandler textHandler = client.textRenderer.getTextHandler();
        List<StringVisitable> lines = textHandler.wrapLines(text, chatHudAccessorInvoker.invokeGetWidth(), text.getStyle(), Text.literal("\uDAFF\uDFFC\uE001\uDB00\uDC06")
                .append(" ").setStyle(prependStyle));
        MutableText out = (MutableText) stringVisitableToText(lines.getFirst());
        for (int i = 1; i < lines.size(); ++i) {
            out.append("\n");
            out.append(stringVisitableToText(lines.get(i)));
        }

        return out;
    }

    @Deprecated
    public static String highlightUser(String message) {
        return message.replaceAll("(?i)(" + McUtils.playerName() + ")", "§e$1§d");
    }

    static class TextVisitors {
        static StringBuilder currentVisit;
        public static final StringVisitable.StyledVisitor<String> PLAIN_VISITOR = (style, asString) -> {
            currentVisit.append(asString.replaceAll("§.", ""));
            return Optional.empty();
        };
        static boolean first = false;
        static boolean afterBlockMarker;
        static TextParseOptions options;
        public static final StringVisitable.StyledVisitor<String> STYLED_VISITOR = (style, asString) -> {
            if (options.extractUsernames && style.getHoverEvent() != null) {
                handleStylesWithHover(style, asString);
            } else {
                handleStyles(style, asString);
            }
            return Optional.empty();
        };

        private static void handleStylesWithHover(Style style, String asString) {
            assert style.getHoverEvent() != null;
            if (style.getHoverEvent().getValue(style.getHoverEvent().getAction()) instanceof Text hoverText) {
                List<Text> siblings = hoverText.getSiblings();
                if (siblings != null) {
                    if (siblings.size() > 2 && siblings.get(1).getString() != null && Objects.requireNonNull(
                            siblings.get(1).getString()).contains("nickname is")) {
                        handleStyles(style.withItalic(false), siblings.getFirst().getString());
                    } else if (!siblings.isEmpty() && siblings.getFirst().getString() != null && siblings.getFirst()
                            .getString().contains("real username is")) {
                        if (siblings.size() > 1) {
                            handleStyles(style.withItalic(false), siblings.get(1).getString());
                        } else {
                            handleStyles(style.withItalic(false), siblings.getFirst().getSiblings().getFirst()
                                    .getString());
                        }
                    } else if (siblings.isEmpty()) {
                        handleStyles(style, asString);
                    }
                }
            } else {
                handleStyles(style, asString);
            }
        }

        private static void handleStyles(Style style, String asString) {
            if (BLOCK_MARKER_PATTERN.matcher(asString).find() && !first) {
                afterBlockMarker = true;
                return;
            }
            // This block is before styles are added so style codes are not added that would be styling empty strings
            if (afterBlockMarker)
                asString = asString.substring(1);
            String toAppend = asString.replaceAll("\\n", options.newline)
                    .replaceAll("§", options.formatCode);
            if (toAppend.isBlank()) {
                afterBlockMarker = false;
                return;
            }

            if (!afterBlockMarker) {
                if (style.getColor() != null) {
                    int colorIndex = 0;
                    for (Formatting format : Formatting.values()) {
                        if (format.getColorValue() != null && format.getColorValue()
                                .equals(style.getColor().getRgb())) {
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
            }
            TextVisitors.currentVisit.append(toAppend);
            if (first) first = false;
        }
    }
}
