package pixlze.guildapi.screens.discord.widgets;

import net.minecraft.client.font.DrawnTextConsumer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.AbstractTextWidget;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.text.MutableText;
import pixlze.guildapi.discord.type.Message;

import java.util.List;

public class ChatMessageWidget extends AbstractTextWidget {
    private static final int PADDING = 4;
    private final Message message;
    private final int shadowColor;
    private int textColor;

    public ChatMessageWidget(Message message, TextRenderer textRenderer, int width, boolean confirmed, boolean isGuild) {
        super(0, 0, width, PADDING + textRenderer.fontHeight + 2 + 10 * message.getContentLines(width - 8 - ScrollableWidget.SCROLLBAR_WIDTH)
                .size(), message.getAuthor().append(" ").append(message.getContent()), textRenderer);
        this.message = message;
        if (!confirmed) {
            this.setTextColor(0xAAAAAA);
        } else {
            confirm();
        }
        shadowColor = isGuild ? 0x24ABFF:0x9003FC;
    }


    public void confirm() {
        this.setTextColor(0xFFFFFF);
    }

    @Override
    public int getHeight() {
        TextRenderer textRenderer = getTextRenderer();
        return PADDING + textRenderer.fontHeight + 2 + 10 * message.getContentLines(this.getWidth() - 8 - ScrollableWidget.SCROLLBAR_WIDTH)
                .size();
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    @Override
    public void draw(DrawnTextConsumer textConsumer) {
        int x = this.getX() + PADDING;
        int y = this.getY() + PADDING;

        textConsumer.text(x, y, message.getAuthor().withColor(shadowColor).asOrderedText());
        List<MutableText> contentLines = message.getContentLines(this.getWidth() - 8 - ScrollableWidget.SCROLLBAR_WIDTH);
        y += 2;
        for (MutableText line : contentLines) {
            y += 10;
            textConsumer.text(x, y, line.withColor(textColor));
        }
    }
}
