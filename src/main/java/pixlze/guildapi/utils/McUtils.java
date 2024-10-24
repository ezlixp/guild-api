package pixlze.guildapi.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.mc.mixin.accessors.ChatHudAccessorInvoker;
import pixlze.guildapi.utils.text.TextUtils;
import pixlze.guildapi.utils.type.Prepend;

public class McUtils {
    public static String playerName() {
        if (GuildApi.isDevelopment()) return "pixlze";
        return player().getName().getString();
    }

    public static String playerUUID() {
        if (GuildApi.isDevelopment()) return "39365bd45c7841de8901c7dc5b7c64c4";
        return player().getUuidAsString();
    }

    public static PlayerEntity player() {
        return mc().player;
    }

    public static MinecraftClient mc() {
        return MinecraftClient.getInstance();
    }

    public static void sendLocalMessage(Text message, MutableText prepend, boolean wynncraftStyle) {
        if (player() == null) {
            GuildApi.LOGGER.error("Tried to send local message but player was null.");
            return;
        }
        ChatHud chatHud = MinecraftClient.getInstance().inGameHud.getChatHud();
        ChatHudAccessorInvoker chatHudAccessorInvoker = (ChatHudAccessorInvoker) chatHud;
        Text withPrepend = Text.empty().append(prepend).append(message);
        if (wynncraftStyle) withPrepend = TextUtils.toBlockMessage(withPrepend, prepend.getStyle());
        Prepend.linesSinceBadge += ChatMessages.breakRenderedChatMessageLines(withPrepend, chatHudAccessorInvoker.invokeGetWidth(), MinecraftClient.getInstance().textRenderer)
                .size();
        player().sendMessage(withPrepend);
    }
}
