package pixlze.guildapi.screens.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import pixlze.guildapi.utils.McUtils;

import java.util.List;
import java.util.Objects;

public abstract class DynamicSizeElementListWidget<E extends DynamicSizeElementListWidget.Entry<E>> extends ContainerWidget {
    private static final Identifier MENU_LIST_BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/menu_list_background.png");
    private static final Identifier INWORLD_MENU_LIST_BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/inworld_menu_list_background.png");
    private final MinecraftClient client;

    public DynamicSizeElementListWidget(Text message, MinecraftClient client) {
        super(0, 0, 0, 0, Text.empty());
        this.client = client;
    }

    @Override
    protected int getContentsHeightWithPadding() {
        int height = 0;
        for (int i = 0; i < getEntryCount(); i++) {
            height += getEntry(i).getHeight() + 4;
        }
        return height;
    }

    public void position(int width, ThreePartsLayoutWidget layout) {
        this.position(width, layout.getContentHeight(), layout.getHeaderHeight());
    }

    public void position(int width, int height, int y) {
        this.setDimensions(width, height);
        this.setPosition(0, y);
        this.refreshScroll();
    }

    @Override
    protected double getDeltaYPerScroll() {
        return 10;
    }

    protected int getEntryCount() {
        return children().size();
    }

    protected E getEntry(int index) {
        return children().get(index);
    }

    abstract public List<E> children();

    protected Entry<E> getEntryAtPosition(double mouseX, double mouseY) {
        if (mouseX < this.getX() || mouseX > this.getRight())
            return null;
        mouseY += this.getScrollY();
        int height = getY();
        for (int i = 0; i < getEntryCount(); i++) {
            if (height <= mouseY && mouseY <= height + getEntry(i).getHeight()) {
                return getEntry(i);
            }
            height += getEntry(i).getHeight() + 4;
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean bl = this.checkScrollbarDragged(mouseX, mouseY, button);
        for (E child : children()) {
            boolean t = child.mouseClicked(mouseX, mouseY, button);
            if (t) this.setFocused(child);
            bl |= t;
        }
        return bl;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int pad = 0;
        int t = getEntryCount();
        this.drawMenuListBackground(context);
        context.enableScissor(this.getX(), this.getY(), this.getRight(), this.getBottom());
        for (int i = 0; i < t; i++) {
            E child = getEntry(i);
            int top = getY() + pad - (int) getScrollY();
            int bottom = top + child.getHeight();
            if (bottom >= getY() && top <= getBottom())
                child.render(context, i, getX(), top, getWidth() - (this.overflows() ? 6:0), child.getHeight(), mouseX, mouseY, delta);
            pad += child.getHeight() + 4;
        }
        context.disableScissor();
        this.drawHeaderAndFooterSeparators(context);
        this.drawScrollbar(context);
    }

    protected void drawMenuListBackground(DrawContext context) {
        Identifier identifier = this.client.world == null ? MENU_LIST_BACKGROUND_TEXTURE:INWORLD_MENU_LIST_BACKGROUND_TEXTURE;
        context.drawTexture(
                RenderLayer::getGuiTextured,
                identifier,
                this.getX(),
                this.getY(),
                (float) this.getRight(),
                (float) (this.getBottom() + (int) this.getScrollY()),
                this.getWidth(),
                this.getHeight(),
                32,
                32
        );
    }

    protected void drawHeaderAndFooterSeparators(DrawContext context) {
        Identifier identifier = McUtils.mc().world == null ? Screen.HEADER_SEPARATOR_TEXTURE:Screen.INWORLD_HEADER_SEPARATOR_TEXTURE;
        Identifier identifier2 = McUtils.mc().world == null ? Screen.FOOTER_SEPARATOR_TEXTURE:Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE;
        context.drawTexture(RenderLayer::getGuiTextured, identifier, this.getX(), this.getY() - 2, 0.0F, 0.0F, this.getWidth(), 2, 32, 2);
        context.drawTexture(RenderLayer::getGuiTextured, identifier2, this.getX(), this.getBottom(), 0.0F, 0.0F, this.getWidth(), 2, 32, 2);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public void setFocused(@Nullable Element focused) {
        if (Objects.equals(focused, getFocused())) return;
        super.setFocused(focused);
    }

    public abstract static class Entry<E extends Entry<E>> implements Element {
        DynamicSizeElementListWidget<E> parent;

        public Entry(DynamicSizeElementListWidget<E> parent) {
            this.parent = parent;
        }

        public abstract int getHeight();

        public abstract void render(DrawContext context, int index, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY, float tickDelta);

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return Objects.equals(parent.getEntryAtPosition(mouseX, mouseY), this);
        }
    }
}
