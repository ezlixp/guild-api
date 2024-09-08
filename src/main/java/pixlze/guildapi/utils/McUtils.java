package pixlze.guildapi.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import pixlze.guildapi.GuildApi;

public class McUtils {
    public static MinecraftClient mc() {
        return MinecraftClient.getInstance();
    }

    public static PlayerEntity player() {
        return mc().player;
    }

    public static String playerName() {
        return player().getName().getString();
    }

    public static void sendLocalMessage(Text message) {
        if (player() == null) {
            GuildApi.LOGGER.error("Tried to send local message but player was null.");
            return;
        }
        player().sendMessage(Text.literal("[Guild API] ").setStyle(Style.EMPTY.withColor(Formatting.GOLD)).append(message));
    }
}
