package pixlze.guildapi.models.guildMessage;

import pixlze.guildapi.models.guildMessage.type.GuildMessage;
import pixlze.guildapi.models.guildMessage.type.HighRankMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuildMessageModel {
    private final Pattern GUILD_PATTERN = Pattern.compile(
            "^§.((\uDAFF\uDFFC\uE006\uDAFF\uDFFF\uE002\uDAFF\uDFFE)|(\uDAFF\uDFFC\uE001\uDB00\uDC06))(§.)? (?<content>.*)$");

    /**
     * Check if a message is a guild message
     *
     * @param message to check
     * @return the guild message instance if it matches, null otherwise
     */
    public GuildMessage isGuildMessage(String message) {
        String m = getContent(message);
        if (m == null) return null;
        for (GuildMessage guildMessage : GuildMessage.values())
            if (guildMessage.getMatcher(m) != null)
                return guildMessage;
        return null;
    }

    /**
     * Check if a message is a high rank message
     *
     * @param message to check
     * @return the high rank message instance if it matches, null otherwise
     */
    public HighRankMessage isHighRankMessage(String message) {
        String m = getContent(message);
        if (m == null) return null;
        for (HighRankMessage highRankMessage : HighRankMessage.values())
            if (highRankMessage.getMatcher(m) != null)
                return highRankMessage;
        return null;
    }

    public String getContent(String message) {
        Matcher matcher = GUILD_PATTERN.matcher(message);
        if (!matcher.find()) return null;
        return matcher.group("content");
    }
}
