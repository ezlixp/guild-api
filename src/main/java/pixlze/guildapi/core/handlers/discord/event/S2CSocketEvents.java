package pixlze.guildapi.core.handlers.discord.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.json.JSONObject;

public class S2CSocketEvents {
    public static Event<Message> DISCORD_MESSAGE = EventFactory.createArrayBacked(Message.class, (listeners) -> (message) -> {
        for (Message listener : listeners) {
            listener.interact(message);
        }
    });

    public static Event<Mirror> WYNN_MIRROR = EventFactory.createArrayBacked(Mirror.class, (listeners) -> (message) -> {
        for (Mirror listener : listeners) {
            listener.interact(message);
        }
    });

    public static Event<Position> PLAYER_POSITION = EventFactory.createArrayBacked(Position.class, (listeners) -> (message) -> {
        for (Position listener : listeners) {
            listener.interact(message);
        }
    });

    public static Event<Hide> PLAYER_HIDE = EventFactory.createArrayBacked(Hide.class, (listeners) -> (message) -> {
        for (Hide listener : listeners) {
            listener.interact(message);
        }
    });

    public interface Message {
        void interact(JSONObject message);
    }

    public interface Mirror {
        void interact(String message);
    }

    public interface Position {
        void interact(JSONObject message);
    }

    public interface Hide {
        void interact(String message);
    }
}
