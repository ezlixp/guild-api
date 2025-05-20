package pixlze.guildapi.screens.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.*;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import pixlze.guildapi.utils.text.TextUtils;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class AllowSectionSignTextField extends ClickableWidget {
    private static final ButtonTextures TEXTURES = new ButtonTextures(
            Identifier.ofVanilla("widget/text_field"), Identifier.ofVanilla("widget/text_field_highlighted")
    );
    public static final int field_32194 = -1;
    public static final int field_32195 = 1;
    private static final int field_32197 = 1;
    private static final int VERTICAL_CURSOR_COLOR = -3092272;
    private static final String HORIZONTAL_CURSOR = "_";
    public static final int DEFAULT_EDITABLE_COLOR = 14737632;
    private static final int field_45354 = 300;
    private final TextRenderer textRenderer;
    private String text = "";
    private int maxLength = 32;
    private boolean drawsBackground = true;
    private boolean focusUnlocked = true;
    private boolean editable = true;
    /**
     * The index of the leftmost character that is rendered on a screen.
     */
    private int firstCharacterIndex;
    private int selectionStart;
    private int selectionEnd;
    private int editableColor = 14737632;
    private int uneditableColor = 7368816;
    @Nullable
    private String suggestion;
    @Nullable
    private Consumer<String> changedListener;
    private Predicate<String> textPredicate = Objects::nonNull;
    private BiFunction<String, Integer, OrderedText> renderTextProvider = (visible, offset) -> {
        if (visible.isEmpty()) return OrderedText.EMPTY;
        return (visitor) -> {
            Style curstyle = computeInheritedStyle(this.text, offset);
            int i = visible.length();

            for (int j = 0; j < i; ++j) {
                char c = visible.charAt(j);
                if (Character.isHighSurrogate(c)) {
                    if (j + 1 >= i) {
                        if (!visitor.accept(j, Style.EMPTY, 65533)) {
                            return false;
                        }
                        break;
                    }

                    char d = visible.charAt(j + 1);
                    if (Character.isLowSurrogate(d)) {
                        if (!visitor.accept(j, Style.EMPTY, Character.toCodePoint(c, d))) {
                            return false;
                        }

                        ++j;
                    } else if (!visitor.accept(j, Style.EMPTY, 65533)) {
                        return false;
                    }
                } else {
                    if (c == '§') {
                        if (j + 1 < i) {
                            Formatting fmt = Formatting.byCode(visible.charAt(j + 1));
                            if (fmt == null) {
                                if (!visitRegularCharacter(curstyle, visitor, j, c)) return false;
                            } else {
                                curstyle = curstyle.withFormatting(fmt);
                                ++j;
                            }
                        } else {
                            if (j + offset + 1 < this.text.length()) {
                                Formatting fmt = Formatting.byCode(this.text.charAt(j + offset + 1));
                                if (fmt != null)
                                    curstyle = curstyle.withFormatting(fmt);
                                if (!visitRegularCharacter(curstyle, visitor, j, c)) return false;
                            } else if (!visitRegularCharacter(curstyle, visitor, j, c)) return false;

                        }
                    } else if (!visitRegularCharacter(curstyle, visitor, j, c)) return false;
                }
            }

            return true;
        };
    };

    @Nullable
    private Text placeholder;
    private long lastSwitchFocusTime = Util.getMeasuringTimeMs();

    public AllowSectionSignTextField(TextRenderer textRenderer, int width, int height, Text text) {
        this(textRenderer, 0, 0, width, height, text);
    }

    public AllowSectionSignTextField(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(x, y, width, height, text);
        this.textRenderer = textRenderer;
    }


    private boolean visitRegularCharacter(Style style, CharacterVisitor visitor, int index, char c) {
        return Character.isSurrogate(c) ? visitor.accept(index, style, 65533):visitor.accept(index, style, c);
    }

    private Style computeInheritedStyle(String fullText, int offset) {
        Style style = Style.EMPTY;
        for (int i = 0; i + 1 <= Math.min(offset, fullText.length() - 1); i++) {
            if (fullText.charAt(i) == '§') {
                char code = fullText.charAt(i + 1);
                Formatting fmt = Formatting.byCode(code);
                if (fmt != null) {
                    style = style.withFormatting(fmt);
                    i++;
                }
            }
        }
        return style;
    }

    private String getRenderedText(String text) {
        StringBuilder out = new StringBuilder();
        for (int j = 0; j < text.length(); ++j) {
            char c = text.charAt(j);
            if (Character.isHighSurrogate(c)) {
                if (j + 1 >= text.length()) {
                    out.append(Character.toString(65533));
                    break;
                }

                char d = text.charAt(j + 1);
                if (Character.isLowSurrogate(d)) {
                    out.append(Character.toString(Character.toCodePoint(c, d)));
                    ++j;
                } else {
                    out.append(Character.toString(65533));
                    break;
                }
            } else {
                if (c == '§') {
                    if (j + 1 < text.length()) {
                        Formatting fmt = Formatting.byCode(text.charAt(j + 1));
                        if (fmt == null) {
                            out.append("&");
                        } else {
                            ++j;
                        }
                    } else out.append("&");
                } else {
                    out.append(c);
                }
            }
        }
        return out.toString();
    }

    private String trimToWidth(Style curStyle, String text, int maxWidth) {
        StringBuilder out = new StringBuilder();
        int curWidth = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (curWidth > maxWidth) break;
            if (c == '§') {
                if (i + 1 < text.length()) {
                    Formatting fmt = Formatting.byCode(text.charAt(i + 1));
                    if (fmt != null) {
                        out.append(c);
                        out.append(text.charAt(i + 1));
                        ++i;
                        curStyle = curStyle.withFormatting(fmt);
                    } else {
                        curWidth += textRenderer.getWidth(Text.literal("&").fillStyle(curStyle));
                        if (curWidth <= maxWidth) out.append(c);
                    }
                } else {
                    curWidth += textRenderer.getWidth(Text.literal("&").fillStyle(curStyle));
                    if (curWidth <= maxWidth) out.append(c);
                }
            } else {
                curWidth += textRenderer.getWidth(Text.literal(String.valueOf(c)).fillStyle(curStyle));
                if (curWidth <= maxWidth) out.append(c);
            }
        }
        return out.toString();
    }

    private String trimToWidth(Style curStyle, String text, int maxWidth, boolean backwards) {
        if (!backwards) return trimToWidth(curStyle, text, maxWidth);
        StringBuilder out = new StringBuilder();
        int curWidth = 0;
        for (int i = text.length() - 1; i >= 0; i--) {
            char c = text.charAt(i);
            if (curWidth > maxWidth) break;
            if (c == '§') {
                if (i - 1 >= 0) {
                    Formatting fmt = Formatting.byCode(text.charAt(i - 1));
                    if (fmt != null) {
                        out.append(c);
                        out.append(text.charAt(i - 1));
                        --i;
                        curStyle = curStyle.withFormatting(fmt);
                    } else {
                        curWidth += textRenderer.getWidth(Text.literal("&").fillStyle(curStyle));
                        if (curWidth <= maxWidth) out.append(c);
                    }
                } else {
                    curWidth += textRenderer.getWidth(Text.literal("&").fillStyle(curStyle));
                    if (curWidth <= maxWidth) out.append(c);
                }
            } else {
                curWidth += textRenderer.getWidth(Text.literal(String.valueOf(c)).fillStyle(curStyle));
                if (curWidth <= maxWidth) out.append(c);
            }
        }
        return out.toString();
    }

    private int getWidth(String text) {
        return textRenderer.getWidth(getRenderedText(text));
    }

    public void setChangedListener(Consumer<String> changedListener) {
        this.changedListener = changedListener;
    }

    public void setRenderTextProvider(BiFunction<String, Integer, OrderedText> renderTextProvider) {
        this.renderTextProvider = renderTextProvider;
    }

    @Override
    protected MutableText getNarrationMessage() {
        Text text = this.getMessage();
        return Text.translatable("gui.narrate.editBox", text, this.text);
    }

    public void setText(String text) {
        if (this.textPredicate.test(text)) {
            if (text.length() > this.maxLength) {
                this.text = text.substring(0, this.maxLength);
            } else {
                this.text = text;
            }

            this.setCursorToEnd(false);
            this.setSelectionEnd(this.selectionStart);
            this.onChanged(text);
        }
    }

    public String getText() {
        return this.text;
    }

    public String getSelectedText() {
        int i = Math.min(this.selectionStart, this.selectionEnd);
        int j = Math.max(this.selectionStart, this.selectionEnd);
        return this.text.substring(i, j);
    }

    public void setTextPredicate(Predicate<String> textPredicate) {
        this.textPredicate = textPredicate;
    }

    private static String stripInvalidChars(String string) {
        StringBuilder stringBuilder = new StringBuilder();

        for (char c : string.toCharArray()) {
            if (StringHelper.isValidChar(c) || c == '§') {
                stringBuilder.append(c);
            }
        }

        return stringBuilder.toString();
    }

    public void write(String text) {
        int i = Math.min(this.selectionStart, this.selectionEnd);
        int j = Math.max(this.selectionStart, this.selectionEnd);
        int k = this.maxLength - this.text.length() - (i - j);
        if (k > 0) {
            String string = stripInvalidChars(text);
            int l = string.length();
            if (k < l) {
                if (Character.isHighSurrogate(string.charAt(k - 1))) {
                    k--;
                }

                string = string.substring(0, k);
                l = k;
            }

            String string2 = new StringBuilder(this.text).replace(i, j, string).toString();
            if (this.textPredicate.test(string2)) {
                this.text = string2;
                this.setSelectionStart(i + l);
                this.setSelectionEnd(this.selectionStart);
                this.onChanged(this.text);
            }
        }
    }

    private void onChanged(String newText) {
        if (this.changedListener != null) {
            this.changedListener.accept(newText);
        }
    }

    private void erase(int offset) {
        if (Screen.hasControlDown()) {
            this.eraseWords(offset);
        } else {
            this.eraseCharacters(offset);
        }
    }

    public void eraseWords(int wordOffset) {
        if (!this.text.isEmpty()) {
            if (this.selectionEnd != this.selectionStart) {
                this.write("");
            } else {
                this.eraseCharactersTo(this.getWordSkipPosition(wordOffset));
            }
        }
    }

    public void eraseCharacters(int characterOffset) {
        this.eraseCharactersTo(this.getCursorPosWithOffset(characterOffset));
    }

    public void eraseCharactersTo(int position) {
        if (!this.text.isEmpty()) {
            if (this.selectionEnd != this.selectionStart) {
                this.write("");
            } else {
                int i = Math.min(position, this.selectionStart);
                int j = Math.max(position, this.selectionStart);
                if (i != j) {
                    String string = new StringBuilder(this.text).delete(i, j).toString();
                    if (this.textPredicate.test(string)) {
                        this.text = string;
                        this.setCursor(i, false);
                    }
                }
            }
        }
    }

    public int getWordSkipPosition(int wordOffset) {
        return this.getWordSkipPosition(wordOffset, this.getCursor());
    }

    private int getWordSkipPosition(int wordOffset, int cursorPosition) {
        return this.getWordSkipPosition(wordOffset, cursorPosition, true);
    }

    private int getWordSkipPosition(int wordOffset, int cursorPosition, boolean skipOverSpaces) {
        int i = cursorPosition;
        boolean bl = wordOffset < 0;
        int j = Math.abs(wordOffset);

        for (int k = 0; k < j; k++) {
            if (!bl) {
                int l = this.text.length();
                i = this.text.indexOf(32, i);
                if (i == -1) {
                    i = l;
                } else {
                    while (skipOverSpaces && i < l && this.text.charAt(i) == ' ') {
                        i++;
                    }
                }
            } else {
                while (skipOverSpaces && i > 0 && this.text.charAt(i - 1) == ' ') {
                    i--;
                }

                while (i > 0 && this.text.charAt(i - 1) != ' ') {
                    i--;
                }
            }
        }

        return i;
    }

    public void moveCursor(int offset, boolean shiftKeyPressed) {
        this.setCursor(this.getCursorPosWithOffset(offset), shiftKeyPressed);
    }

    private int getCursorPosWithOffset(int offset) {
        return Util.moveCursor(this.text, this.selectionStart, offset);
    }

    public void setCursor(int cursor, boolean shiftKeyPressed) {
        this.setSelectionStart(cursor);
        if (!shiftKeyPressed) {
            this.setSelectionEnd(this.selectionStart);
        }

        this.onChanged(this.text);
    }

    public void setSelectionStart(int cursor) {
        this.selectionStart = MathHelper.clamp(cursor, 0, this.text.length());
        this.updateFirstCharacterIndex(this.selectionStart);
    }

    public void setCursorToStart(boolean shiftKeyPressed) {
        this.setCursor(0, shiftKeyPressed);
    }

    public void setCursorToEnd(boolean shiftKeyPressed) {
        this.setCursor(this.text.length(), shiftKeyPressed);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.isNarratable() && this.isFocused()) {
            switch (keyCode) {
                case 259:
                    if (this.editable) {
                        this.erase(-1);
                    }

                    return true;
                case 260:
                case 264:
                case 265:
                case 266:
                case 267:
                default:
                    if (Screen.isSelectAll(keyCode)) {
                        this.setCursorToEnd(false);
                        this.setSelectionEnd(0);
                        return true;
                    } else if (Screen.isCopy(keyCode)) {
                        MinecraftClient.getInstance().keyboard.setClipboard(this.getSelectedText());
                        return true;
                    } else if (Screen.isPaste(keyCode)) {
                        if (this.isEditable()) {
                            this.write(MinecraftClient.getInstance().keyboard.getClipboard());
                        }

                        return true;
                    } else {
                        if (Screen.isCut(keyCode)) {
                            MinecraftClient.getInstance().keyboard.setClipboard(this.getSelectedText());
                            if (this.isEditable()) {
                                this.write("");
                            }

                            return true;
                        }

                        return false;
                    }
                case 261:
                    if (this.editable) {
                        this.erase(1);
                    }

                    return true;
                case 262:
                    if (Screen.hasControlDown()) {
                        this.setCursor(this.getWordSkipPosition(1), Screen.hasShiftDown());
                    } else {
                        this.moveCursor(1, Screen.hasShiftDown());
                    }

                    return true;
                case 263:
                    if (Screen.hasControlDown()) {
                        this.setCursor(this.getWordSkipPosition(-1), Screen.hasShiftDown());
                    } else {
                        this.moveCursor(-1, Screen.hasShiftDown());
                    }

                    return true;
                case 268:
                    this.setCursorToStart(Screen.hasShiftDown());
                    return true;
                case 269:
                    this.setCursorToEnd(Screen.hasShiftDown());
                    return true;
            }
        } else {
            return false;
        }
    }

    public boolean isActive() {
        return this.isNarratable() && this.isFocused() && this.isEditable();
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!this.isActive()) {
            return false;
        } else if (StringHelper.isValidChar(chr) || chr == '§') {
            if (this.editable) {
                this.write(Character.toString(chr));
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        int i = MathHelper.floor(mouseX) - this.getX();
        if (this.drawsBackground) {
            i -= 4;
        }


        String string = trimToWidth(computeInheritedStyle(this.text, this.firstCharacterIndex), this.text.substring(this.firstCharacterIndex), this.getInnerWidth());
        this.setCursor(trimToWidth(computeInheritedStyle(this.text, this.firstCharacterIndex), string, i).length() + this.firstCharacterIndex, Screen.hasShiftDown());
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.isVisible()) {
            if (this.drawsBackground()) {
                Identifier identifier = TEXTURES.get(this.isNarratable(), this.isFocused());
                context.drawGuiTexture(RenderLayer::getGuiTextured, identifier, this.getX(), this.getY(), this.getWidth(), this.getHeight());
            }

            int colour = this.editable ? this.editableColor:this.uneditableColor;
            if (TextUtils.isFormatting(this.text, this.firstCharacterIndex - 1))
                ++this.firstCharacterIndex;
            int selectionStartIdx = this.selectionStart - this.firstCharacterIndex;
            String visibleText = trimToWidth(computeInheritedStyle(this.text, this.firstCharacterIndex), this.text.substring(this.firstCharacterIndex), this.getInnerWidth());
            boolean pointerVisible = selectionStartIdx >= 0 && selectionStartIdx <= visibleText.length();
            boolean blinkingPointer = this.isFocused() && (Util.getMeasuringTimeMs() - this.lastSwitchFocusTime) / 300L % 2L == 0L && pointerVisible;
            int textStartX = this.drawsBackground ? this.getX() + 4:this.getX();
            int textStartY = this.drawsBackground ? this.getY() + (this.height - 8) / 2:this.getY();
            int curTextStart = textStartX;
            int selectionEndIdx = MathHelper.clamp(this.selectionEnd - this.firstCharacterIndex, 0, visibleText.length());
            if (!visibleText.isEmpty()) {
                String beforePointer = pointerVisible ? visibleText.substring(0, selectionStartIdx):visibleText;
                curTextStart = context.drawTextWithShadow(this.textRenderer, this.renderTextProvider.apply(beforePointer, this.firstCharacterIndex), textStartX, textStartY, colour);
            }

            boolean renderPointerAsLine = this.selectionStart < this.text.length() || this.text.length() >= this.getMaxLength();
            int pointerX = curTextStart;
            if (!pointerVisible) {
                // pointer is outside of visisble range
                pointerX = selectionStartIdx > 0 ? textStartX + this.width:textStartX;
            } else if (renderPointerAsLine) {
                pointerX = curTextStart - 1;
                curTextStart--;
            }

            if (!visibleText.isEmpty() && pointerVisible && selectionStartIdx < visibleText.length()) {
                // this renders the second half of the text, after the pointer
                context.drawTextWithShadow(this.textRenderer, this.renderTextProvider.apply(visibleText.substring(selectionStartIdx), this.selectionStart), curTextStart, textStartY, colour);
            }

            // placeholder text drawn
            if (this.placeholder != null && visibleText.isEmpty() && !this.isFocused()) {
                context.drawTextWithShadow(this.textRenderer, this.placeholder, curTextStart, textStartY, colour);
            }

            if (!renderPointerAsLine && this.suggestion != null) {
                context.drawTextWithShadow(this.textRenderer, this.suggestion, pointerX - 1, textStartY, Colors.GRAY);
            }

            if (blinkingPointer) {
                if (renderPointerAsLine) {
                    Style beforeStyle = computeInheritedStyle(this.text, this.selectionStart);
                    MatrixStack stack = context.getMatrices();
                    stack.push();
                    if (beforeStyle.isItalic()) {
                        stack.translate(pointerX, textStartY + 4.5, 0);
                        stack.multiply(RotationAxis.POSITIVE_Z.rotation(0.245f));
                        stack.translate(-pointerX, -textStartY - 4.5, 0);
                    }
                    context.fill(RenderLayer.getGuiOverlay(), pointerX, textStartY - 1, pointerX + 1, textStartY + 1 + 9, beforeStyle.getColor() != null ? beforeStyle.getColor().getRgb() + 0xFF000000:-3092272);
                    stack.pop();
                } else {
                    context.drawTextWithShadow(this.textRenderer, Text.literal("_").fillStyle(computeInheritedStyle(this.text, this.selectionStart).withObfuscated(false).withStrikethrough(false).withUnderline(false)), pointerX, textStartY, colour);
                }
            }

            if (selectionEndIdx != selectionStartIdx) {
                int selectionEndX = textStartX + getWidth(visibleText.substring(0, selectionEndIdx));
                if (selectionStartIdx > 0 && selectionStartIdx < visibleText.length() && visibleText.charAt(selectionStartIdx - 1) == '§') {
                    if (Formatting.byCode(visibleText.charAt(selectionStartIdx)) != null) {
                        if (selectionStartIdx < selectionEndIdx)
                            selectionEndX += 2 * getWidth("&");
                    }
                }
                if (selectionEndIdx > 0 && selectionEndIdx < visibleText.length() && visibleText.charAt(selectionEndIdx - 1) == '§') {
                    if (Formatting.byCode(visibleText.charAt(selectionEndIdx)) != null) {
                        selectionEndX -= getWidth("&");
                    }
                }
                this.drawSelectionHighlight(context, pointerX, textStartY - 1, selectionEndX - 1, textStartY + 1 + 9);
            }
        }
    }

    private void drawSelectionHighlight(DrawContext context, int x1, int y1, int x2, int y2) {
        if (x1 < x2) {
            int i = x1;
            x1 = x2;
            x2 = i;
        }

        if (y1 < y2) {
            int i = y1;
            y1 = y2;
            y2 = i;
        }

        if (x2 > this.getX() + this.width) {
            x2 = this.getX() + this.width;
        }

        if (x1 > this.getX() + this.width) {
            x1 = this.getX() + this.width;
        }

        context.fill(RenderLayer.getGuiTextHighlight(), x1, y1, x2, y2, Colors.BLUE);
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        if (this.text.length() > maxLength) {
            this.text = this.text.substring(0, maxLength);
            this.onChanged(this.text);
        }
    }

    private int getMaxLength() {
        return this.maxLength;
    }

    public int getCursor() {
        return this.selectionStart;
    }

    public boolean drawsBackground() {
        return this.drawsBackground;
    }

    public void setDrawsBackground(boolean drawsBackground) {
        this.drawsBackground = drawsBackground;
    }

    public void setEditableColor(int editableColor) {
        this.editableColor = editableColor;
    }

    public void setUneditableColor(int uneditableColor) {
        this.uneditableColor = uneditableColor;
    }

    @Override
    public void setFocused(boolean focused) {
        if (this.focusUnlocked || focused) {
            super.setFocused(focused);
            if (focused) {
                this.lastSwitchFocusTime = Util.getMeasuringTimeMs();
            }
        }
    }

    private boolean isEditable() {
        return this.editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public int getInnerWidth() {
        return this.drawsBackground() ? this.width - 8:this.width;
    }

    public void setSelectionEnd(int index) {
        this.selectionEnd = MathHelper.clamp(index, 0, this.text.length());
        this.updateFirstCharacterIndex(this.selectionEnd);
    }

    private void updateFirstCharacterIndex(int cursor) {
        if (this.textRenderer != null) {
            this.firstCharacterIndex = Math.min(this.firstCharacterIndex, this.text.length());
            while (this.firstCharacterIndex < this.text.length() - 1 && this.text.charAt(this.firstCharacterIndex) == '§' && Formatting.byCode(this.text.charAt(this.firstCharacterIndex + 1)) != null)
                ++this.firstCharacterIndex;
            int i = this.getInnerWidth();
            String visibleText = trimToWidth(computeInheritedStyle(this.text, this.firstCharacterIndex), this.text.substring(this.firstCharacterIndex), i);
            int endingAbsolutePos = visibleText.length() + this.firstCharacterIndex;
            if (cursor == this.firstCharacterIndex) {
                // if the cursor is at the first character, move one width to the right
                this.firstCharacterIndex = this.firstCharacterIndex - trimToWidth(Style.EMPTY, this.text.substring(0, this.firstCharacterIndex + visibleText.length()), i, true).length();
            }

            if (cursor > endingAbsolutePos) {
                // if cursor is beyond the end of the current visible text, set the cursor to be the rightmost visible thing
                this.firstCharacterIndex += cursor - endingAbsolutePos;
            } else if (cursor <= this.firstCharacterIndex) {
                // if the cursor is before the visible text, set the cursor to be the first visible thing
                this.firstCharacterIndex = cursor;
            }

            this.firstCharacterIndex = MathHelper.clamp(this.firstCharacterIndex, 0, this.text.length());
        }
    }

    public void setFocusUnlocked(boolean focusUnlocked) {
        this.focusUnlocked = focusUnlocked;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setSuggestion(@Nullable String suggestion) {
        this.suggestion = suggestion;
    }

    public int getCharacterX(int index) {
        return index > this.text.length() ? this.getX():this.getX() + getWidth(this.text.substring(0, index));
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, this.getNarrationMessage());
    }

    public void setPlaceholder(@Nullable Text placeholder) {
        this.placeholder = placeholder;
    }
}
