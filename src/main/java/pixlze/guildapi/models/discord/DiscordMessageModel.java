package pixlze.guildapi.models.discord;

import java.util.HashSet;
import java.util.Locale;

public class DiscordMessageModel {
    private final HashSet<String> blocked = new HashSet<>();

    public boolean isBlocked(String username) {
        return blocked.contains(username.toLowerCase(Locale.ROOT));
    }

    public void block(String username) {
        blocked.add(username.toLowerCase(Locale.ROOT));
    }
}
