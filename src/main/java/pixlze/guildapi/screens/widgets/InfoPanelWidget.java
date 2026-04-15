package pixlze.guildapi.screens.widgets;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class InfoPanelWidget extends ClickableWidget {
    public static final Identifier BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/container/generic_54.png");
    private final static int ITEMS_PER_PAGE = 10;

    private Screen parent;
    private final int headerHeight = 13;
    private int x, y, width, height, page, highlightColour, firstHighlightColour;
    private double valthresh;

    private final String title;

    private final List<Entry> entries;

    private final ButtonWidget closeButton, prevButton, nextButton;

    public InfoPanelWidget(int x, int y, int width, int height, String title, ButtonWidget.PressAction onClose, List<Entry> entries) {
        this(x, y, width, height, title, onClose, entries, 1);
    }

    public InfoPanelWidget(int x, int y, int width, int height, String title, ButtonWidget.PressAction onClose, List<Entry> entries, double valthresh) {
        super(x, y, width, height, Text.literal("Raid reward list."));
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.valthresh = valthresh;

        this.entries = entries;

        this.page = 0;

        this.title = title;
        this.closeButton = ButtonWidget.builder(Text.literal("×"), onClose).build();
        closeButton.setWidth(9);
        closeButton.setHeight(9);
        this.prevButton = ButtonWidget.builder(Text.literal("<"), (button) -> {
            this.changePage(-1);
        }).build();
        prevButton.setWidth(20);
        prevButton.setHeight(11);
        prevButton.active = false;
        this.nextButton = ButtonWidget.builder(Text.literal(">"), (button) -> {
            this.changePage(1);
        }).build();
        nextButton.setWidth(20);
        nextButton.setHeight(11);
        this.changePage(0);
    }

    public void setHighlightColour(int highlightColour) {
        this.highlightColour = highlightColour;
    }

    public void setFirstHighlightColour(int firstHighlightColour) {
        this.firstHighlightColour = firstHighlightColour;
    }

    public void update(String key, double delta) {
        int rmvidx = -1;
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            if (entry.key.equals(key)) {
                entry.value += delta;
                if (entry.value < valthresh)
                    rmvidx = i;
                break;
            }
        }
        if (rmvidx != -1)
            entries.remove(rmvidx);
        this.changePage(0);
//      entries.sort(Comparator.comparingDouble(a -> -a.value));
    }

    private List<Entry> getPageEntries() {
        List<Entry> out = new ArrayList<>();
        for (int i = ITEMS_PER_PAGE * page; i < Math.min(ITEMS_PER_PAGE * (page + 1), entries.size()); i++) {
            out.add(entries.get(i));
        }
        return out;
    }

    private int getMaxPage() {
        return Math.max(0, (int) (Math.ceil((double) entries.size() / ITEMS_PER_PAGE) - 1));
    }

    public void changePage(int dx) {
        int newpage = Math.clamp(page + dx, 0, getMaxPage());
        prevButton.active = newpage != 0;
        nextButton.active = newpage != getMaxPage();
        page = newpage;
    }

    private void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, getX(), getY(), 0.0f, 0.0f, getWidth(), getHeight() - 4, 146, 276);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, getX(), getY() + getHeight() - 4, 0.0f, 235, getWidth(), 4, 146, 276);
    }

    private void renderHeader(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.drawText(McUtils.mc().textRenderer, Text.literal(this.title), getX() + 5, getY() + 6, 0xFF444444, false);
        context.fill(getX() + getWidth() - 22, getY() + 7, getX() + getWidth() - 17, getY() + 12, highlightColour);
        closeButton.setX(this.getX() + getWidth() - closeButton.getWidth() - 5);
        closeButton.setY(this.getY() + 5);
        closeButton.render(context, mouseX, mouseY, deltaTicks);
    }

    private void renderFooter(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        prevButton.setX(this.getX() + 19);
        prevButton.setY(this.getY() + this.getHeight() - prevButton.getHeight() - 2);

        nextButton.setX(this.getX() + this.getWidth() - prevButton.getWidth() - 20);
        nextButton.setY(this.getY() + this.getHeight() - prevButton.getHeight() - 2);

        prevButton.render(context, mouseX, mouseY, deltaTicks);
        nextButton.render(context, mouseX, mouseY, deltaTicks);

    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        renderBackground(context, mouseX, mouseY, deltaTicks);
        renderHeader(context, mouseX, mouseY, deltaTicks);
        int y = getY() + 20;
        if (!Managers.Net.guild.isDisabled()) {
            for (Entry reward : getPageEntries()) {
                context.drawText(McUtils.mc().textRenderer, Text.literal(reward.key + ": " + reward.value), getX() + 7, y, 0xFF555555, false);
                y += 10;
            }
        } else {
            context.drawText(McUtils.mc().textRenderer, Text.literal("§cPlease login"), getX() + 19, y + getHeight() / 2 - 30, 0xFF7FB5B5, false);
        }
        renderFooter(context, mouseX, mouseY, deltaTicks);
    }

    public void onSlotDrawn(DrawContext context, Slot slot) {
        Text customName = slot.inventory.getStack(slot.getIndex()).getComponents().get(DataComponentTypes.CUSTOM_NAME);
        if (customName == null) return;
        String name = TextUtils.parsePlain(customName).toLowerCase();
        boolean doHighlight = false;
        boolean first = true;
        ArrayList<Entry> t = new ArrayList<>(entries);
        for (Entry entry : t) {
            if (name.equals(entry.key.toLowerCase())) {
                doHighlight = true;
                break;
            }
            first = false;
        }
        if (doHighlight) {
            context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, first ? firstHighlightColour:highlightColour);
        }
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void forEachChild(Consumer<ClickableWidget> consumer) {
        GuildApi.LOGGER.info("hi");
    }

    @Override
    public void setFocused(boolean focused) {

    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public void onClick(Click click, boolean doubled) {
        GuildApi.LOGGER.info("onClick");
        if (closeButton.isMouseOver(click.x(), click.y()))
            closeButton.onClick(click, doubled);
        if (prevButton.isMouseOver(click.x(), click.y()))
            prevButton.onClick(click, doubled);
        if (nextButton.isMouseOver(click.x(), click.y()))
            nextButton.onClick(click, doubled);
    }

    @Override
    protected void onDrag(Click click, double offsetX, double offsetY) {
        // We don't simply increment the position by the offsets because of rounding errors making any offset below 1 not move the widget at all
        // making it possible to move the mouse all the way across the screen without moving the widget.
        int padx = (int) (click.x() - offsetX - this.getX());
        int pady = (int) (click.y() - offsetY - this.getY());
        this.setX((int) (click.x() - padx));
        this.setY((int) (click.y() - pady));
    }

    @Override
    public boolean isFocused() {
        return true;
    }

    public static class Entry {
        String key;
        public double value;

        public Entry(String key, double value) {
            this.key = key;
            this.value = value;
        }
    }
}
