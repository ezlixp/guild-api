package pixlze.guildapi.screens.discord.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import pixlze.guildapi.discord.type.Message;
import pixlze.guildapi.screens.discord.DiscordChatScreen;
import pixlze.guildapi.screens.widgets.DynamicSizeElementListWidget;
import pixlze.guildapi.utils.McUtils;

public class DiscordChatWidget extends DynamicSizeElementListWidget<DiscordChatWidget.Entry> {

    public DiscordChatWidget(MinecraftClient client, int width, DiscordChatScreen discordChatScreen) {
        super(client, width, discordChatScreen.layout.getContentHeight(), discordChatScreen.layout.getHeaderHeight());
    }

    public void addMessage(Message m, boolean confirmed, boolean isGuild) {
        boolean sticky = this.getScrollY() == this.getMaxScrollY();
        this.addEntry(Entry.create(m, this.width, confirmed, isGuild));
        if (sticky) this.setScrollY(this.getMaxScrollY());
    }

    @Override
    public void position(int width, int height, int y) {
        this.setDimensions(width, height);
        this.setPosition(0, y);
        for (int i = 0; i < getEntryCount(); i++) {
            getEntry(i).message.setWidth(width);
        }
        this.setScrollY(this.getScrollY() > getMaxScrollY() ? getMaxScrollY():getScrollY());
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
        if (getEntryCount() == 0) {
            TextRenderer textRenderer = McUtils.mc().textRenderer;
            OrderedText text = Text.of("Nothing to see here...").asOrderedText();
            context.drawText(textRenderer, text, getX() + width / 2 - textRenderer.getWidth(text) / 2, getY() + height / 2 - McUtils.mc().textRenderer.fontHeight / 2, 0xAAAAAA, false);
        }
    }

    @Override
    protected double getDeltaYPerScroll() {
        return 8;
    }

    public static class Entry extends DynamicSizeElementListWidget.Entry<DiscordChatWidget.Entry> {
        private final ChatMessageWidget message;

        private Entry(Message m, int width, boolean confirmed, boolean isGuild) {
            message = new ChatMessageWidget(m, McUtils.mc().textRenderer, width, confirmed, isGuild);
        }

        public static Entry create(Message m, int width, boolean confirmed, boolean isGuild) {
            return new Entry(m, width, confirmed, isGuild);
        }

        public void confirm() {
            message.confirm();
        }

        @Override
        public int getHeight() {
            return message.getHeight();
        }

        @Override
        public void render(DrawContext context, int index, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY, float tickDelta) {
            message.setPosition(x, y);
            message.render(context, mouseX, mouseY, tickDelta);
        }

        @Override
        public void setFocused(boolean focused) {

        }

        @Override
        public boolean isFocused() {
            return false;
        }
    }
}
