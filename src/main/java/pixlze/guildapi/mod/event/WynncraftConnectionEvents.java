package pixlze.guildapi.mod.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.MinecraftClient;

public interface WynncraftConnectionEvents {
    Event<WynncraftConnectionEvents> JOIN = EventFactory.createArrayBacked(WynncraftConnectionEvents.class, (listeners) -> (client) -> {
        for (WynncraftConnectionEvents listener : listeners) {
            listener.interact(client);
        }
    });

    void interact(MinecraftClient client);
}
