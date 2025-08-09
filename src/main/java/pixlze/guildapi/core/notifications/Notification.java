package pixlze.guildapi.core.notifications;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.utils.McUtils;

import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Notification<E extends Trigger> {
    private final Function<E, Boolean> doTrigger;
    private final MutableText display;
    public final Object trigger;
    public final String displayText;

    private Notification(Function<E, Boolean> doTrigger, String display, Object trigger) {
        this.doTrigger = doTrigger;
        this.display = Text.literal(display);
        this.displayText = display;
        this.trigger = trigger;
    }

    public static Notification<Trigger.CHAT> ofChat(String triggerRegex, String display) {
        try {
            Pattern triggerPattern = Pattern.compile(triggerRegex);
            return new Notification<>((packet) -> triggerPattern.matcher(packet.message).find(), display, triggerPattern);
        } catch (PatternSyntaxException exception) {
            GuildApi.LOGGER.warn("invalid regex: {} {}", triggerRegex, exception.getMessage());
        }
        return null;
    }

    public void apply(E packet) {
        if (doTrigger.apply(packet)) {
            McUtils.sendTitleMessage(display);
        }
    }
}

// use notification manager to accept chat events, and distribute to notifications