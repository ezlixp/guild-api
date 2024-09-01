package pixlze.pixutils.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;
import pixlze.pixutils.mixin.accessors.ClickableWidgetAccessorInvoker;

public record ClickableChild<T extends ClickableWidget>(T child) implements Element {

    public boolean getActive() {
        return child.active;
    }

    public T getChild() {
        return child;
    }

    public boolean getVisible() {
        return child.visible;
    }

    public int getWidth() {
        return child.getWidth();
    }

    public int getHeight() {
        return child.getHeight();
    }

    public int getX() {
        return child.getX();
    }

    public int getY() {
        return child.getY();
    }

    public int getBottom() {
        return child.getBottom();
    }

    public boolean isFocused() {
        return child.isFocused();
    }

    public void setFocused(boolean value) {
        child.setFocused(value);
    }

    public void playDownSound(SoundManager soundManager) {
        child.playDownSound(soundManager);
    }

    public void onClick(double mouseX, double mouseY) {
        child.onClick(mouseX, mouseY);
    }

    public boolean charTyped(char chr, int modifiers) {
        return child.charTyped(chr, modifiers);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return child.keyPressed(keyCode, scanCode, modifiers);
    }


    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!child.visible) {
            return;
        }
        ((ClickableWidgetAccessorInvoker) child).setHovered(mouseX >= child.getX() && mouseY >= child.getY() && mouseX < child.getX() + child.getWidth() && mouseY < child.getY() + child.getHeight());
        ((ClickableWidgetAccessorInvoker) child).invokeRenderWidget(context, mouseX, mouseY, delta);
        ((ClickableWidgetAccessorInvoker) child).getTooltipState().render(child.isMouseOver(mouseX, mouseY), child.isFocused(), child.getNavigationFocus());
    }


}
