package pixlze.guildapi.mc.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.mc.event.PlayerInfoChangedEvents;
import pixlze.guildapi.mc.event.PlayerTeleport;
import pixlze.guildapi.mc.event.ScreenOpen;
import pixlze.guildapi.mc.event.WynnChatMessage;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPacketListenerMixin {
    @Inject(method = "onGameMessage", at = @At("HEAD"))
    private void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        if (!MinecraftClient.getInstance().isOnThread()) return;
        if (!packet.overlay() && Managers.Connection.onWynncraft()) {
            WynnChatMessage.EVENT.invoker().interact(packet.content());
        }
    }

    @Inject(method = "onOpenScreen", at = @At("HEAD"))
    private void onOpenScreen(OpenScreenS2CPacket packet, CallbackInfo ci) {
        if (!MinecraftClient.getInstance().isOnThread()) return;
        ScreenOpen.EVENT.invoker().interact(packet.getScreenHandlerType(), packet.getName());
    }

    // for world
    @Inject(method = "onPlayerList", at = @At("HEAD"))
    private void onPlayerList(PlayerListS2CPacket packet, CallbackInfo ci) {
        if (!MinecraftClient.getInstance().isOnThread()) return;
        if (!Managers.Connection.onWynncraft()) return;
        for (PlayerListS2CPacket.Entry entry : packet.getEntries()) {
            for (PlayerListS2CPacket.Action action : packet.getActions()) {
                if (action == PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME) {
                    if (entry.displayName() == null) continue;
                    PlayerInfoChangedEvents.DISPLAY.invoker().displayChanged(entry.profileId(), entry.displayName());
                }
            }
        }
    }

    // for hub
    @Inject(method = "onPlayerListHeader", at = @At("HEAD"))
    private void onPlayerListHeader(PlayerListHeaderS2CPacket packet, CallbackInfo ci) {
        if (!MinecraftClient.getInstance().isOnThread()) return;
        PlayerInfoChangedEvents.FOOTER.invoker().footerChanged(packet.footer());
    }

    @Inject(method = "onPlayerPositionLook", at = @At("HEAD"))
    private void onPlayerPositionLook(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
        if (!MinecraftClient.getInstance().isOnThread()) return;
        if (!packet.getFlags().isEmpty()) return;
        PlayerTeleport.EVENT.invoker().playerTeleported(new Vec3d(packet.getX(), packet.getY(), packet.getZ()));
    }
}
