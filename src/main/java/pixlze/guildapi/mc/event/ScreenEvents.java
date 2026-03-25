package pixlze.guildapi.mc.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;


public class ScreenEvents {
    public static final Event<Change> CHANGE = EventFactory.createArrayBacked(Change.class, (listeners) -> (screen) -> {
        for (Change listener : listeners) {
            listener.screenChanged(screen);
        }
    });

    public static final Event<Resize> RESIZE = EventFactory.createArrayBacked(Resize.class, (listeners) -> (screen) -> {
        for (Resize listener : listeners) {
            listener.screenResized(screen);
        }
    });

    public static final Event<Slot> SLOT = EventFactory.createArrayBacked(Slot.class, (listeners) -> (context, slot) -> {
        for (Slot listener : listeners) {
            listener.slotDrawn(context, slot);
        }
    });

    public interface Change {
        void screenChanged(Screen screen);
    }

    public interface Resize {
        void screenResized(Screen screen);
    }

    public interface Slot {
        void slotDrawn(DrawContext context, net.minecraft.screen.slot.Slot slot);
    }
}
