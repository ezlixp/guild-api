package pixlze.guildapi.mc.mixin.accessors;

import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(SystemToast.class)
public interface SystemToastInvoker {
    @Invoker("<init>")
    static SystemToast create(SystemToast.Type type, Text title, List<OrderedText> lines, int width) {
        throw new UnsupportedOperationException("This is a mixin invoker");
    }
}
