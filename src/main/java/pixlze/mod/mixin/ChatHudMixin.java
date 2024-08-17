package pixlze.mod.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
    @Shadow
    private final List<ChatHudLine> messages = new ArrayList<>();

    public List<ChatHudLine> getMessages() {
        return messages;
    }
}
