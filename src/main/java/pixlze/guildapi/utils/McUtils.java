package pixlze.guildapi.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.utils.type.Prepend;

public class McUtils {
    public static String playerName() {
        return player().getName().getString();
    }

    public static PlayerEntity player() {
        return mc().player;
    }

    public static MinecraftClient mc() {
        return MinecraftClient.getInstance();
    }

    public static void sendLocalMessage(Text message, MutableText prepend) {
        if (player() == null) {
            GuildApi.LOGGER.error("Tried to send local message but player was null.");
            return;
        }
        player().sendMessage(Text.empty().append(prepend).append(message));
    }
}
