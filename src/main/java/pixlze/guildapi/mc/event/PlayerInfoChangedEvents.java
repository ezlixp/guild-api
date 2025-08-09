package pixlze.guildapi.mc.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class PlayerInfoChangedEvents {
    public static final Event<Footer> FOOTER = EventFactory.createArrayBacked(Footer.class, (listeners) -> (footer) -> {
        for (Footer listener : listeners) {
            listener.footerChanged(footer);
        }
    });
    public static final Event<Display> DISPLAY = EventFactory.createArrayBacked(Display.class, (listeners) -> (uuid, display) -> {
        for (Display listener : listeners) {
            listener.displayChanged(uuid, display);
        }
    });
    public static final Event<Position> POSITION = EventFactory.createArrayBacked(Position.class, (listeners) -> (newpos) -> {
        for (Position listener : listeners) {
            listener.positionChanged(newpos);
        }
    });

    public interface Footer {
        void footerChanged(Text footer);
    }

    public interface Display {
        void displayChanged(UUID uuid, Text display);
    }

    public interface Position {
        void positionChanged(Vec3d newpos);
    }
}
