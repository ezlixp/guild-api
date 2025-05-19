package pixlze.guildapi.core.notifications;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import pixlze.guildapi.utils.McUtils;

import java.util.function.Function;

public class Notification<E extends Trigger> {
    private final Function<E, Boolean> doTrigger;
    private final MutableText display;
    public final Object trigger;
    public final String displayText;

    private Notification(Function<E, Boolean> doTrigger, String display, String trigger) {
        this.doTrigger = doTrigger;
        this.display = Text.literal(display);
        this.displayText = display;
        this.trigger = trigger;
    }

    public static Notification<Trigger.CHAT> ofChat(String triggerText, String display) {
        return new Notification<>((message) -> message.message.contains(triggerText), display, triggerText);
    }

    public void apply(E packet) {
        if (doTrigger.apply(packet)) {
            McUtils.sendTitleMessage(display);
        }
    }
}

// use notification manager to accept chat events, and distribute to notifications