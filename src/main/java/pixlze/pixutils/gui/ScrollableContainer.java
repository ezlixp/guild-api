package pixlze.pixutils.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Stack;

public class ScrollableContainer extends ScrollableWidget {
    //    private final ArrayList<Drawable> drawables = new ArrayList<>();
    private final ArrayList<ClickableChild<?>> clickables = new ArrayList<>();
    private final Stack<Element> removeables = new Stack<>();
    private final int[] boxDimensions;
    private final float boxAlpha;

    public ScrollableContainer(int x, int y, int width, int height, Text message, float boxAlpha) {
        super(x, y, width, height, message);
        this.boxDimensions = new int[]{this.getX(), this.getY(), this.getWidth(), this.getHeight()};
        this.boxAlpha = boxAlpha;
    }

    public ScrollableContainer(int x, int y, int width, int height, Text message, float boxAlpha, int scrollBarX) {
        super(x, y, width, height, message);
        this.boxDimensions = new int[]{this.getX(), this.getY(), this.getWidth(), this.getHeight()};
        this.boxAlpha = boxAlpha;
    }


    public void addClickableChild(ClickableChild<?> child) {
        clickables.add(child);
    }

    public void queueRemove(Element child) {
        removeables.push(child);
    }

    public void clearChildren() {
//        this.drawables.clear();
        this.clickables.clear();
    }

    private boolean widgetClicked(ClickableChild<?> target, double mouseX, double mouseY) {
        return target.getActive() && target.getVisible() && mouseX >= (double) target.getX() && mouseY + this.getScrollY() >= (double) target.getY() && mouseX < (double) (target.getX() + target.getWidth()) && mouseY + this.getScrollY() < (double) (target.getY() + target.getHeight());
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= this.getX() && mouseX <= this.getRight() && mouseY >= this.getY() && mouseY <= this.getBottom() && button == 0) {
            for (ClickableChild<?> clickable : clickables) {
                clickable.setFocused(false);
                if (widgetClicked(clickable, mouseX, mouseY)) {
                    clickable.playDownSound(MinecraftClient.getInstance().getSoundManager());
                    clickable.setFocused(true);
                    clickable.onClick(mouseX, mouseY);
                }
            }
        }
        while (!removeables.isEmpty()) {
            Element toRemove = removeables.pop();
            if (toRemove instanceof ClickableChild) {
                clickables.remove(toRemove);
            }
//            if (toRemove instanceof Drawable) {
//                drawables.remove(toRemove);
//            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (ClickableChild<?> clickable : clickables) {
            if (clickable.isFocused())
                clickable.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (ClickableChild<?> clickable : clickables) {
            if (clickable.isFocused())
                clickable.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void drawBox(DrawContext context) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1, 1, 1, this.boxAlpha);
        assert MinecraftClient.getInstance().currentScreen != null;
        super.drawBox(context, this.boxDimensions[0], this.boxDimensions[1], this.boxDimensions[2], this.boxDimensions[3]);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }

    @Override
    protected int getContentsHeight() {
        int maxHeight = 0;
        for (ClickableChild<?> clickable : clickables) {
            maxHeight = Math.max(maxHeight, clickable.getBottom() - this.getY());
        }
        return maxHeight;
    }

    @Override
    protected double getDeltaYPerScroll() {
        return 10;
    }

    @Override
    protected void renderContents(DrawContext context, int mouseX, int mouseY, float delta) {
        for (ClickableChild<?> children : clickables) {
            children.render(context, mouseX, mouseY + (int) this.getScrollY(), delta);
        }
    }
}
