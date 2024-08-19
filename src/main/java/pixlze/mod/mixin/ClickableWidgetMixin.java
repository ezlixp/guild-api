package pixlze.mod.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.tooltip.TooltipState;
import net.minecraft.client.gui.widget.ClickableWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClickableWidget.class)
public class ClickableWidgetMixin implements Drawable {
    @Shadow
    private final TooltipState tooltip = new TooltipState();
    @Shadow
    public boolean visible;
    @Shadow
    protected boolean hovered;
    @Shadow
    protected int width;
    @Shadow
    protected int height;

    @Shadow
    public int getX() {
        return 0;
    }

    @Shadow
    public int getY() {
        return 0;
    }

    @Shadow
    public boolean isHovered() {
        return false;
    }

    @Shadow
    public boolean isFocused() {
        return false;
    }

    @Shadow
    public ScreenRect getNavigationFocus() {
        return null;
    }

    @Shadow
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {

    }


    @Override
    public final void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!this.visible) {
            return;
        }
        this.hovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
        this.renderWidget(context, mouseX, mouseY, delta);
        this.tooltip.render(this.isHovered(), this.isFocused(), this.getNavigationFocus());
    }
}
