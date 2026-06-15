package pixlze.guildapi.mc.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.util.math.Vec3d;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pixlze.guildapi.core.components.Handlers;
import pixlze.guildapi.mc.event.PlayerInfoChangedEvents;
import pixlze.guildapi.mc.event.WynnChatMessage;
import pixlze.guildapi.utils.type.Prepend;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPacketListenerMixin {
    @Shadow
    @Nullable
    protected abstract Entity createEntity(EntitySpawnS2CPacket packet);

    @Inject(method = "onGameMessage", at = @At("HEAD"))
    private void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        if (!MinecraftClient.getInstance().isOnThread()) return;
        if (!packet.overlay() && Handlers.Connection.onWynncraft()) {
            Prepend.lastBadge = "";
            WynnChatMessage.EVENT.invoker().interact(packet.content());
        }
    }

//    @Inject(method = "onEntitySpawn", at = @At("HEAD"))
//    private void onEntitySpawn(EntitySpawnS2CPacket packet, CallbackInfo ci) {
//        if (!MinecraftClient.getInstance().isOnThread()) return;
//        Entity entity = this.createEntity(packet);
//        if (entity != null) {
//            entity.onSpawnPacket(packet);
//        }
//    }
//
//    @Inject(method = "onEntityAttributes", at = @At("HEAD"))
//    private void onEntityAttributes(EntityAttributesS2CPacket packet, CallbackInfo ci) {
//        if (!MinecraftClient.getInstance().isOnThread()) return;
//
//    }


    // for world
    @Inject(method = "onPlayerList", at = @At("HEAD"))
    private void onPlayerList(PlayerListS2CPacket packet, CallbackInfo ci) {
        if (!MinecraftClient.getInstance().isOnThread()) return;
        if (!Handlers.Connection.onWynncraft()) return;
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

    // for /class
    @Inject(method = "onPlayerPositionLook", at = @At("HEAD"))
    private void onPlayerPositionLook(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
        if (!MinecraftClient.getInstance().isOnThread()) return;
        if (!packet.relatives().isEmpty()) return;

        PlayerInfoChangedEvents.POSITION.invoker()
                .positionChanged(new Vec3d(packet.change().position().x, packet.change().position().y, packet.change()
                        .position().z));
    }
}
