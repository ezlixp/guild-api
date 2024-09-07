package pixlze.guildapi.mod.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface WynncraftConnectionEvents {
    Event<WynncraftConnectionEvents> JOIN = EventFactory.createArrayBacked(WynncraftConnectionEvents.class, (listeners) -> () -> {
        for (WynncraftConnectionEvents listener : listeners) {
            listener.interact();
        }
    });

    void interact();
}
