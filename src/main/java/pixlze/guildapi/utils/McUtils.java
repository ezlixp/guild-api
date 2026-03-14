package pixlze.guildapi.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.components.Managers;
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

    public static synchronized void sendLocalMessage(Text message, MutableText prepend, boolean wynncraftStyle) {
        if (player() == null) {
            Managers.Net.join.addTask(() -> mc().execute(() -> sendLocalMessage(message, prepend, wynncraftStyle)));
            GuildApi.LOGGER.warn("Tried to send local message but player was null. Queueing message...");
            return;
        }
        if (!RenderSystem.isOnRenderThread()) {
            GuildApi.LOGGER.warn("Send local message was not called on render thread: {}", TextUtils.parsePlain(message));
            mc().execute(() -> sendLocalMessage(message, prepend, wynncraftStyle));
            return;
        }
        Text withPrepend = Text.empty().append(prepend).append(message);
        if (wynncraftStyle)
            withPrepend = TextUtils.toBlockMessage(withPrepend, prepend.getStyle());
        Prepend.linesSinceBadge += ChatMessages.breakRenderedChatMessageLines(withPrepend, McUtils.getChatWidth(), MinecraftClient.getInstance().textRenderer)
                .size();
        player().sendMessage(withPrepend, false);
    }

    public static int getChatWidth() {
        // low 40, high 320
        return MathHelper.floor(mc().options.getChatWidth().getValue() * 280.0 + 40.0);
    }

    public static void sendTitleMessage(Text message) {
        mc().inGameHud.setTitle(message);
        mc().inGameHud.setTitleTicks(5, 20, 10);
    }
}
