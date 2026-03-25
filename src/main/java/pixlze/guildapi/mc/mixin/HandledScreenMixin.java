package pixlze.guildapi.mc.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pixlze.guildapi.mc.event.ScreenEvents;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {

    @Inject(method = "drawSlot", at = @At("HEAD"))
    protected void drawSlot(DrawContext context, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        ScreenEvents.SLOT.invoker().slotDrawn(context, slot);
    }
}
