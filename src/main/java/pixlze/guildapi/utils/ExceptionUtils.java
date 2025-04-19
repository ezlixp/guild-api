package pixlze.guildapi.utils;

import net.minecraft.text.Text;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.utils.type.Prepend;

import java.util.function.Consumer;

public class ExceptionUtils {
    public static Consumer<String> defaultFailed(String name, boolean feedback) {
        return (error) -> {
            if (feedback)
                McUtils.sendLocalMessage(Text.literal("§c" + name + " failed. Reason: " + error), Prepend.DEFAULT.get(), false);
            GuildApi.LOGGER.error("{} error: {}", name, error);
        };
    }

    public static void defaultException(String name, Exception e) {
        McUtils.sendLocalMessage(Text.literal("§cSomething went wrong. Check logs for more details."), Prepend.DEFAULT.get(), false);
        GuildApi.LOGGER.error("{} exception: {} {}", name, e, e.getMessage());
    }
}
