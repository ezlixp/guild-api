package pixlze.guildapi.screens.discord.widgets;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AbstractTextWidget;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.features.discord.DiscordBridgeFeature;

import java.util.List;

public class ChatMessageWidget extends AbstractTextWidget {
    private static final int PADDING = 4;
    private final String author;
    private final String content;
    private final int colour;

    public ChatMessageWidget(String author, String content, TextRenderer textRenderer, int width, boolean confirmed, boolean isGuild) {
        super(0, 0, width, PADDING + textRenderer.fontHeight + 2 + 10 * textRenderer.wrapLines(Text.literal(content), width - 8 - ScrollableWidget.SCROLLBAR_WIDTH)
                .size(), Text.literal(author).append(" ").append(content), textRenderer);
        this.author = author;
        this.content = content;
        if (!confirmed) {
            this.setTextColor(0xAAAAAA);
        }
        colour = isGuild ? 0x24ABFF:0x9003FC;
    }


    public void confirm() {
        this.setTextColor(0xFFFFFF);
    }

    @Override
    public int getHeight() {
        TextRenderer textRenderer = getTextRenderer();
        return PADDING + textRenderer.fontHeight + 2 + 10 * textRenderer.wrapLines(Text.literal(content), this.getWidth() - 8 - ScrollableWidget.SCROLLBAR_WIDTH)
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

        context.drawTextWithShadow(textRenderer, Text.literal(((DiscordBridgeFeature) Managers.Feature.getFeatureInstance(DiscordBridgeFeature.class)).highlightMessage(author)).withColor(colour)
                .asOrderedText(), x, y, this.getTextColor());
        List<OrderedText> contentLines = textRenderer.wrapLines(Text.literal(((DiscordBridgeFeature) Managers.Feature.getFeatureInstance(DiscordBridgeFeature.class)).highlightMessage(content)), this.getWidth() - 8 - ScrollableWidget.SCROLLBAR_WIDTH);
        y += 2;
        for (OrderedText line : contentLines) {
            y += 10;
            context.drawTextWithShadow(textRenderer, line, x, y, this.getTextColor());
        }
    }
}
