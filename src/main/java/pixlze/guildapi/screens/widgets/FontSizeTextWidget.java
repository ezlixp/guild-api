package pixlze.guildapi.screens.widgets;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Language;

public class FontSizeTextWidget extends TextWidget {
    private float horizontalAlignment = 0.5f;
    private final int height;

    public FontSizeTextWidget(int height, Text message, TextRenderer textRenderer) {
        super(message, textRenderer);
        this.height = height;
    }

    private TextWidget align(float horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
        return this;
    }

    @Override
    public TextWidget alignLeft() {
        return this.align(0.0F);
    }

    @Override
    public TextWidget alignCenter() {
        return this.align(0.5F);
    }

    @Override
    public TextWidget alignRight() {
        return this.align(1.0F);
    }

    private OrderedText trim(Text text, int width) {
        TextRenderer textRenderer = this.getTextRenderer();
        StringVisitable stringVisitable = textRenderer.trimToWidth(text, width - textRenderer.getWidth(ScreenTexts.ELLIPSIS));
        return Language.getInstance().reorder(StringVisitable.concat(stringVisitable, ScreenTexts.ELLIPSIS));
    }


    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        Text text = this.getMessage();
        TextRenderer textRenderer = this.getTextRenderer();
        int widgetWidth = this.getWidth();
        int textWidth = textRenderer.getWidth(text);
        int k = this.getX() + Math.round(this.horizontalAlignment * (float) (widgetWidth - textWidth));
        int l = this.getY() + (this.getHeight() - 9) / 2;
        OrderedText orderedText = textWidth > widgetWidth ? this.trim(text, widgetWidth):text.asOrderedText();

        MatrixStack stack = context.getMatrices();
        stack.push();

        float scale = ((float) height) / textRenderer.fontHeight;
        stack.translate(getX() + (float) widgetWidth / 2, getY(), 0);
        stack.scale(scale, scale, 1f);
        stack.translate(-(getX() + (float) widgetWidth / 2) + 0.5f, -getY(), 0);

        context.drawText(textRenderer, orderedText, k, l, this.getTextColor(), false);
        context.drawBorder(getX(), getY(), getWidth() - 1, getHeight(), 0xFFFFFFFF);

        stack.pop();
    }

}

