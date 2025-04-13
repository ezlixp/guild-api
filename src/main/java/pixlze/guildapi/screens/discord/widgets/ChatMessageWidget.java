package pixlze.guildapi.screens.discord.widgets;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AbstractTextWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Language;

public class ChatMessageWidget extends AbstractTextWidget {
    private float horizontalAlignment = 0F;

    public ChatMessageWidget(Text author, Text content, TextRenderer textRenderer, int width) {
        super(0, 0, width, 9 + textRenderer.getWrappedLinesHeight(content, width), author.copy().append(" ").append(content), textRenderer);
    }

    public ChatMessageWidget setTextColor(int textColor) {
        super.setTextColor(textColor);
        return this;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        Text text = this.getMessage();
        TextRenderer textRenderer = this.getTextRenderer();
        int i = this.getWidth();
        int j = textRenderer.getWidth(text);
        int k = 4 + this.getX() + Math.round(this.horizontalAlignment * (float) (i - j));
        int l = this.getY() + (this.getHeight() - 9) / 2;
        OrderedText orderedText = j > i ? this.trim(text, i):text.asOrderedText();
        context.drawTextWithShadow(textRenderer, orderedText, k, l, this.getTextColor());
    }

    private OrderedText trim(Text text, int width) {
        TextRenderer textRenderer = this.getTextRenderer();
        StringVisitable stringVisitable = textRenderer.trimToWidth(text, width - textRenderer.getWidth(ScreenTexts.ELLIPSIS));
        return Language.getInstance().reorder(StringVisitable.concat(stringVisitable, ScreenTexts.ELLIPSIS));
    }
}
