package pixlze.guildapi.screens.discord.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import pixlze.guildapi.screens.discord.DiscordScreen;
import pixlze.guildapi.utils.McUtils;

import java.util.List;

public class DiscordChatWidget extends ElementListWidget<DiscordChatWidget.Entry> {

    public DiscordChatWidget(MinecraftClient client, int width, DiscordScreen discordScreen) {
        super(client, width, discordScreen.layout.getContentHeight(), discordScreen.layout.getHeaderHeight(), 25);
    }

    public void addOption(Screen toScreen) {
        this.addEntry(Entry.create(toScreen));
    }

    public static class Entry extends ElementListWidget.Entry<DiscordChatWidget.Entry> {
        private final List<ClickableWidget> widgets;

        private Entry(Screen toScreen) {
            widgets = List.of(ButtonWidget.builder(toScreen.getTitle(), (button) -> {
                McUtils.mc().setScreen(toScreen);
            }).width(210).build());
        }

        public static Entry create(Screen toScreen) {
            return new Entry(toScreen);
        }

        @Override
        public List<ClickableWidget> selectableChildren() {
            return widgets;
        }

        @Override
        public List<ClickableWidget> children() {
            return widgets;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            for (ClickableWidget child : children()) {
                child.setPosition(x, y);
                child.setHeight(entryHeight);
                child.render(context, mouseX, mouseY, tickDelta);
            }
        }
    }
}
