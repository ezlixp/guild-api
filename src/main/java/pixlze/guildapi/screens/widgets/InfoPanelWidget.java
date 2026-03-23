package pixlze.guildapi.screens.widgets;

import com.google.gson.JsonElement;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.type.Prepend;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class InfoPanelWidget extends ClickableWidget {
    // like browser thingie
    private final static int ITEMS_PER_PAGE = 10;

    private final int headerHeight = 15;
    private int x, y, width, height, page;
    private final String title, sortMember, endpoint;
    private final List<Entry> entries = new ArrayList<>();
    private final Function<JsonElement, Entry> elementConverter;
    private final ButtonWidget closeButton, prevButton, nextButton;

    public InfoPanelWidget(String sortMember, int x, int y, int width, int height, String title, String endpoint, ButtonWidget.PressAction onClose, Function<JsonElement, Entry> elementConverter) {
        super(x, y, width, height, Text.literal("Raid reward list."));
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.page = 0;

        this.sortMember = sortMember;
        this.title = title;
        this.endpoint = endpoint;
        this.elementConverter = elementConverter;
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
    }

    public void update(String key, double delta) {
        for (Entry reward : entries) {
            if (reward.key.equals(key)) {
                reward.value += delta;
            }
        }
        changePage(0);
        entries.sort(Comparator.comparingDouble(a -> -a.value));
    }

    private List<Entry> getPageEntries() {
        List<Entry> out = new ArrayList<>();
        for (int i = ITEMS_PER_PAGE * page; i < Math.min(ITEMS_PER_PAGE * (page + 1), entries.size()); i++) {
            out.add(entries.get(i));
        }
        return out;
    }

    private int getMaxPage() {
        return (int) (Math.ceil((double) entries.size() / ITEMS_PER_PAGE) - 1);
    }

    private void changePage(int dx) {
        int newpage = Math.clamp(page + dx, 0, getMaxPage());
        prevButton.active = newpage != 0;
        nextButton.active = newpage != getMaxPage();
        page = newpage;
    }

    public void refresh() {
        entries.clear();
        Managers.Net.guild.getList(endpoint + Managers.Net.guild.guildId, false, sortMember).whenComplete((res, exception) -> {
            if (exception != null) {
                McUtils.sendLocalMessage(Text.literal("§cSomething went wrong. Check logs for more details."), Prepend.DEFAULT.get(), false);
                GuildApi.LOGGER.error("Reward list widget error: {} {}", exception, exception.getMessage());
                return;
            }
            for (JsonElement reward : res) {
                entries.add(elementConverter.apply(reward));
            }
            changePage(0);
        });
    }

    private void renderHeader(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.drawText(McUtils.mc().textRenderer, Text.literal(this.title), getX() + 2, getY() + 2, 0xFF7FB5B5, false);
        context.drawHorizontalLine(getX(), getX() + getWidth(), getY() + 13, 0xFF000000);
        closeButton.setX(this.getX() + getWidth() - closeButton.getWidth() - 2);
        closeButton.setY(this.getY() + 2);
        closeButton.render(context, mouseX, mouseY, deltaTicks);
    }

    private void renderFooter(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.drawHorizontalLine(getX(), getX() + getWidth(), getY() + getHeight() - 20, 0xFF000000);
        prevButton.setX(this.getX() + 19);
        prevButton.setY(this.getY() + this.getHeight() - prevButton.getHeight() - 4);

        nextButton.setX(this.getX() + this.getWidth() - prevButton.getWidth() - 20);
        nextButton.setY(this.getY() + this.getHeight() - prevButton.getHeight() - 4);

        prevButton.render(context, mouseX, mouseY, deltaTicks);
        nextButton.render(context, mouseX, mouseY, deltaTicks);

    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0xFFFFFFFF);
        renderHeader(context, mouseX, mouseY, deltaTicks);
        int y = getY() + 20;
        for (Entry reward : getPageEntries()) {
            context.drawText(McUtils.mc().textRenderer, Text.literal(reward.key + ": " + reward.value), getX() + 2, y, 0xFF7FB5B5, false);
            y += 10;
        }
        renderFooter(context, mouseX, mouseY, deltaTicks);
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
        double value;

        public Entry(String key, double value) {
            this.key = key;
            this.value = value;
        }
    }
}
