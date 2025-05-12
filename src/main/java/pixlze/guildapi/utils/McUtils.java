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
    public static String devName = "pixlze";
    public static String devUUID = "39365bd4-5c78-41de-8901-c7dc5b7c64c4";

    public static String playerName() {
        if (GuildApi.isDevelopment() || GuildApi.isTesting()) return devName;
        return mc().getSession().getUsername();
    }

    public static String playerUUID() {
        if (GuildApi.isDevelopment() || GuildApi.isTesting()) return devUUID;
        return mc().getSession().getUuidOrNull().toString();
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
        player().sendMessage(withPrepend, false);
    }

    public static void sendTitleMessage(Text message) {
        mc().inGameHud.setTitle(message);
        mc().inGameHud.setTitleTicks(20, 500, 20);
    }
}
