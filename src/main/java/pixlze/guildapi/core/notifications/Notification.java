package pixlze.guildapi.core.notifications;

import com.google.gson.JsonElement;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.utils.McUtils;

import java.util.function.Function;

public class Notification<E extends NotificationTrigger> {
    private final Function<E, Boolean> doTrigger;
    private final MutableText display;
    public final JsonElement trigger;
    public final String displayText;

    private Notification(Function<E, Boolean> trigger, String display, JsonElement triggerElement) {
        this.doTrigger = trigger;
        this.display = Text.literal(display);
        this.displayText = display;
        this.trigger = triggerElement;
    }

    public static Notification<NotificationTrigger.CHAT> ofChat(String triggerText, String display) {
        return new Notification<>((message) -> message.message.contains(triggerText), display, Managers.Json.toJsonElement(Managers.Json.escapeUnsafeJsonChars(triggerText)));
    }

    public void apply(E packet) {
        if (doTrigger.apply(packet)) {
            McUtils.sendTitleMessage(display);
        }
    }
}

// use notification manager to accept chat events, and distribute to notifications