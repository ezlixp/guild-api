package pixlze.guildapi.core.handlers.discord.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.json.JSONObject;

public class C2SDiscordEvents {
    public static Event<Message> MESSAGE = EventFactory.createArrayBacked(Message.class, (listeners) -> (message) -> {
        for (Message listener : listeners) {
            listener.interact(message);
        }
    });

    public interface Message {
        void interact(JSONObject message);
    }
}
