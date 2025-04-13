package pixlze.guildapi.screens.discord.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import pixlze.guildapi.screens.discord.DiscordScreen;
import pixlze.guildapi.screens.widgets.DynamicSizeElementListWidget;
import pixlze.guildapi.utils.McUtils;

public class DiscordChatWidget extends DynamicSizeElementListWidget<DiscordChatWidget.Entry> {

    public DiscordChatWidget(MinecraftClient client, int width, DiscordScreen discordScreen) {
        super(client, width, discordScreen.layout.getContentHeight(), discordScreen.layout.getHeaderHeight());
        this.addMessage(Text.of("pixlze"), Text.of("testing"));
    }

    public void addMessage(Text author, Text content) {
        this.addEntry(Entry.create(author, content, this.width));
    }

    public static class Entry extends DynamicSizeElementListWidget.Entry<DiscordChatWidget.Entry> {
        private final ChatMessageWidget message;

        private Entry(Text author, Text content, int width) {
            message = new ChatMessageWidget(author, content, McUtils.mc().textRenderer, width);
        }

        public static Entry create(Text author, Text content, int width) {
            return new Entry(author, content, width);
        }

        @Override
        public int getHeight() {
            return message.getHeight();
        }

        @Override
        public void render(DrawContext context, int index, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY, float tickDelta) {
            context.fill(x, y, x + entryWidth, y + entryHeight, 0xFFEEFFEE);
            message.setPosition(x, y);
            message.setWidth(entryWidth);
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
