package pixlze.guildapi.models.worldState.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import pixlze.guildapi.models.worldState.type.WorldState;

public interface WorldStateEvents {
    Event<WorldStateEvents> CHANGE = EventFactory.createArrayBacked(WorldStateEvents.class, (listeners) -> (newState) -> {
        for (WorldStateEvents listener : listeners) {
            listener.changed(newState);
        }
    });

    void changed(WorldState newState);
}