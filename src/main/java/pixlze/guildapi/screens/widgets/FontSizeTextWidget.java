package pixlze.guildapi.screens.widgets;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import org.joml.Matrix3x2fStack;
import pixlze.guildapi.utils.McUtils;

public class FontSizeTextWidget extends TextWidget {
    private int height;

    public FontSizeTextWidget(int height, Text message, TextRenderer textRenderer) {
        super(message, textRenderer);
        this.height = height;
    }


    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    private OrderedText trim(Text text, int width) {
        TextRenderer textRenderer = this.getTextRenderer();
        StringVisitable stringVisitable = textRenderer.trimToWidth(text, width - textRenderer.getWidth(ScreenTexts.ELLIPSIS));
        return Language.getInstance().reorder(StringVisitable.concat(stringVisitable, ScreenTexts.ELLIPSIS));
    }

    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        Matrix3x2fStack stack = context.getMatrices();
        stack.pushMatrix();

        // TODO: centering, do smth in consturctor to auto align x or do it before constructor
        float scale = ((float) height) / McUtils.mc().textRenderer.fontHeight;
        stack.translate(getX() + (float) this.getWidth() / 2, getY() + (float) this.getHeight() / 2, stack);
        stack.scale(scale, scale, stack);
        stack.translate(-(getX() + (float) this.getWidth() / 2) + 0.5f, -getY() - (float) this.getHeight() / 2 + 0.5f, stack);

        super.renderWidget(context, mouseX, mouseY, delta);

        stack.popMatrix();
    }
}

