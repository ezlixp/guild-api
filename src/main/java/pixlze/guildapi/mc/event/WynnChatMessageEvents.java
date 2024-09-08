package pixlze.guildapi.mc.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.text.Text;


public interface WynnChatMessageEvents {
    Event<WynnChatMessageEvents> CHAT = EventFactory.createArrayBacked(WynnChatMessageEvents.class,
                                                                       (listeners) -> (message) -> {
                                                                           for (WynnChatMessageEvents listener :
                                                                                   listeners) {
                                                                               listener.interact(message);
                                                                           }
                                                                       });

    void interact(Text message);
}
