package pixlze.mod.mixin.accessors;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipState;
import net.minecraft.client.gui.widget.ClickableWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClickableWidget.class)
public interface ClickableWidgetAccessorInvoker {

    @Accessor("hovered")
    void setHovered(boolean value);

    @Accessor("tooltip")
    TooltipState getTooltipState();

    @Invoker("renderWidget")
    void renderWidget(DrawContext context, int mouseX, int mouseY, float delta);


}
