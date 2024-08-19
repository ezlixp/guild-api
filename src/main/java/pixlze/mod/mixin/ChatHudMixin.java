package pixlze.mod.mixin;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
    @Shadow
    private final List<ChatHudLine> messages = new ArrayList<>();

    @Unique
    public List<ChatHudLine> getMessages() {
        return messages;
    }
}
