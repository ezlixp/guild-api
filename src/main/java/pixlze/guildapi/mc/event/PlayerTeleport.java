package pixlze.guildapi.mc.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.math.Vec3d;

public interface PlayerTeleport {
    Event<PlayerTeleport> EVENT = EventFactory.createArrayBacked(PlayerTeleport.class, (listeners) -> (pos) -> {
        for (PlayerTeleport listener : listeners) {
            listener.playerTeleported(pos);
        }
    });

    void playerTeleported(Vec3d pos);
}
