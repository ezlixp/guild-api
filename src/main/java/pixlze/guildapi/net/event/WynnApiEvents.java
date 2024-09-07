package pixlze.guildapi.net.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface WynnApiEvents {
    Event<WynnApiEvents> SUCCESS = EventFactory.createArrayBacked(WynnApiEvents.class, (listeners) -> () -> {
        for (WynnApiEvents listener : listeners) {
            listener.interact();
        }
    });

    void interact();

}
