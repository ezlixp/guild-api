package pixlze.guildapi.mc.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.mc.event.WynnChatMessageEvents;

@Mixin(ClientPlayNetworkHandler.class)
public class ChatPacketListenerMixin {
    @Inject(method = "onGameMessage", at = @At("HEAD"))
    private void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        if (!packet.overlay() && Managers.Connection.onWynncraft()) {
            GuildApi.LOGGER.info("message {}", packet.content());
            WynnChatMessageEvents.CHAT.invoker().interact(packet.content());
        }
    }
}
