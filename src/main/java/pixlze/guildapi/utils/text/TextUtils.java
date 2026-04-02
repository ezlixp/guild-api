package pixlze.guildapi.utils.text;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextHandler;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import pixlze.guildapi.GuildApi;
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
        if (!currentPart.getString().isEmpty() || splitted.size() < 2)
            splitted.add(currentPart);
        return splitted;
    }

    /**
     * @param text     what to wrap
     * @param maxWidth width to wrap to
     * @return the text but wrapped with newline characters
     * @deprecated in favor of textrendere.wraplines
     */
    @Deprecated
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
        TextVisitors.prevCodes = null;
        TextVisitors.firstOnNewLine = false;
        text.visit(TextVisitors.STYLED_VISITOR, Style.EMPTY);
        return TextVisitors.currentVisit.toString();
    }

    public static String parsePlain(StringVisitable text) {
        TextVisitors.currentVisit = new StringBuilder();
        text.visit(TextVisitors.PLAIN_VISITOR, Style.EMPTY);
        return TextVisitors.currentVisit.toString();
    }

    public static Text toBlockMessage(Text text, Style prependStyle) {
        if (!RenderSystem.isOnRenderThread())
            GuildApi.LOGGER.warn("To block message was not called on render thread: {}", TextUtils.parsePlain(text));
        TextHandler textHandler = McUtils.mc().textRenderer.getTextHandler();
        List<MutableText> lines = new ArrayList<>();
        textHandler.wrapLines(text, McUtils.getChatWidth(), text.getStyle(), (textx, endsInNewline) -> {
            if (endsInNewline)
                lines.add(Text.empty().append(Text.literal("\uDAFF\uDFFC\uE001\uDB00\uDC06")
                        .append(" ").setStyle(prependStyle)).append(stringVisitableToText(textx)));
            else lines.add(Text.empty().append(stringVisitableToText(textx)));
        });
        MutableText out = lines.getFirst();
        for (int i = 1; i < lines.size(); ++i) {
            out.append("\n");
            out.append(stringVisitableToText(lines.get(i)));
        }

        return out;
    }

    public static List<MutableText> wrapToMutableText(Text text, int maxWidth) {
        List<MutableText> lines = new ArrayList<>();
        TextHandler textHandler = McUtils.mc().textRenderer.getTextHandler();
        textHandler.wrapLines(text, maxWidth, text.getStyle(), (textx, lastine) -> {
            lines.add(Text.empty().append(stringVisitableToText(textx)));
        });
        return lines;

    }

    public static Text stringVisitableToText(StringVisitable visitable) {
        MutableText out = Text.empty();
        visitable.visit((style, asString) -> {
            out.append(Text.literal(asString).setStyle(style));
            return Optional.empty();
        }, Style.EMPTY);
        return out;
    }

    public static Style fontOf(Identifier of) {
        return Style.EMPTY.withFont(new StyleSpriteSource.Font(of));
    }

    /**
     * @param message the message to highlight
     * @return the message with yellow formatting codes around the users in game name
     * @deprecated in favor of the highlight words config option
     */
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
        static boolean firstOnNewLine;
        static ArrayList<String> prevCodes;
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
            if (style.getHoverEvent() instanceof HoverEvent.ShowText(
                    Text value
            )) {
                List<Text> siblings = value.getSiblings();
                if (siblings != null) {
                    if (siblings.size() > 2 && siblings.get(1).getString() != null && Objects.requireNonNull(
                            siblings.get(1).getString()).contains("nickname is")) {
                        handleStyles(style.withItalic(false), siblings.getFirst().getString());
                    } else if (!siblings.isEmpty() && siblings.getFirst().getString() != null && (siblings.getFirst()
                            .getString().contains("real username is") || siblings.getFirst().getString().contains("real name is"))) {
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
            if (toAppend.isEmpty()) {
                afterBlockMarker = false;
                firstOnNewLine = true;
                return;
            }

            if (!afterBlockMarker) {
                ArrayList<String> curCodes = new ArrayList<>();
                if (style.getColor() != null) {
                    String t = getColourCode(style);
                    curCodes.add(t);
                }
                if (style.isBold()) {
                    curCodes.add(options.formatCode + Formatting.BOLD.getCode());
                }
                if (style.isItalic()) {
                    curCodes.add(options.formatCode + Formatting.ITALIC.getCode());
                }
                if (style.isUnderlined()) {
                    curCodes.add(options.formatCode + Formatting.UNDERLINE.getCode());
                }
                if (style.isStrikethrough()) {
                    curCodes.add(options.formatCode + Formatting.STRIKETHROUGH.getCode());
                }
                if (style.isObfuscated()) {
                    curCodes.add(options.formatCode + Formatting.OBFUSCATED.getCode());
                }
                for (String code : curCodes)
                    if (!firstOnNewLine || !prevCodes.contains(code))
                        TextVisitors.currentVisit.append(code);
                prevCodes = curCodes;
            } else {
                afterBlockMarker = false;
            }
            TextVisitors.currentVisit.append(toAppend);
            if (first) first = false;
            if (firstOnNewLine) firstOnNewLine = false;
        }

        private static @NotNull String getColourCode(Style style) {
            int colorIndex = 0;
            for (Formatting format : Formatting.values()) {
                if (format.getColorValue() != null && format.getColorValue()
                        .equals(style.getColor().getRgb())) {
                    colorIndex = format.getColorIndex();
                    break;
                }
            }
            Formatting formatting = Formatting.byColorIndex(colorIndex);
            assert formatting != null;
            String t = options.formatCode + formatting.getCode();
            return t;
        }
    }
}
