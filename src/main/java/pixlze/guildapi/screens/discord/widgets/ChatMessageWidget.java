package pixlze.guildapi.screens.discord.widgets;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AbstractTextWidget;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.List;

public class ChatMessageWidget extends AbstractTextWidget {
    private static final int PADDING = 4;
    private final Text author;
    private final Text content;

    public ChatMessageWidget(Text author, Text content, TextRenderer textRenderer, int width, boolean confirmed) {
        super(0, 0, width, PADDING + textRenderer.fontHeight + 2 + 10 * textRenderer.wrapLines(content, width - 8 - ScrollableWidget.SCROLLBAR_WIDTH)
                .size(), author.copy().append(" ").append(content), textRenderer);
        this.author = author;
        this.content = content;
        if (!confirmed) {
            this.setTextColor(0xAAAAAA);
        }
    }


    public void confirm() {
        this.setTextColor(0xFFFFFF);
    }

    @Override
    public int getHeight() {
        TextRenderer textRenderer = getTextRenderer();
        return PADDING + textRenderer.fontHeight + 2 + 10 * textRenderer.wrapLines(content, this.getWidth() - 8 - ScrollableWidget.SCROLLBAR_WIDTH)
                .size();
    }

    public ChatMessageWidget setTextColor(int textColor) {
        super.setTextColor(textColor);
        return this;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        TextRenderer textRenderer = this.getTextRenderer();

        int x = this.getX() + PADDING;
        int y = this.getY() + PADDING;

        context.drawTextWithShadow(textRenderer, author.copy().withColor(0x1524ABFF)
                .asOrderedText(), x, y, this.getTextColor());
        List<OrderedText> contentLines = textRenderer.wrapLines(content, this.getWidth() - 8 - ScrollableWidget.SCROLLBAR_WIDTH);
        y += 2;
        for (OrderedText line : contentLines) {
            y += 10;
            context.drawTextWithShadow(textRenderer, line, x, y, this.getTextColor());
        }
    }
}
