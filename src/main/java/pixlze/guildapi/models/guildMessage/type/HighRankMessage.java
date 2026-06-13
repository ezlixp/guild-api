package pixlze.guildapi.models.guildMessage.type;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum HighRankMessage {
    // Eco
    TERRITORY_BONUS_MODIFY("^§.(?<username>\\S+?)§. set §.(?<bonus>.+?)§. to level §.(?<level>\\d+?)§. on §.(?<territory>.*)$"),
    TERRITORY_BONUS_REMOVE("^§.(?<username>\\S+?)§. removed §.(?<changed>.+?)§. from §.(?<territory>.*)$"),
    TERRITORY_BONUS_MASS_MODIFY("^§.(?<username>\\S+?)§. changed §.(?<amount>\\d+) (?<changed>\\w+)§. on §3(?<territory>.*)$"),
    TERRITORY_LOADOUT_APPLY("^§.(?<username>\\S+?)§. applied the loadout §(?<loadout>.+?)§. on §.(?<territory>.*)$"),
    TERRITORY_RESOURCE_WARNING("^Territory §.(?<territory>.+?)§. is \\w+ more resources than it can store!$"),
    TERRITORY_RESOURCE_STABILISE("^Territory §.(?<territory>.+?)§. production has stabilised$"),
    GLOBAL_TAX("^§.(?<username>\\S+?)§. changed the global tax to §.(?<percent>\\d+)%$"),
    // Guild bank
    GUILD_BANK("^§.(?<username>\\S+?)§. (?<action>\\w+) ?§.(?<item>.+?)§. ?(?:to|from) ?the ?Guild ?Bank ?\\(§.High ?Ranked§.\\)$"),
    // Guild tome found
    TOME_FOUND("^§.A Guild Tome§. has been found and added to the Guild Rewards$"),

    // Unsure if these are accurate or if available to all (maybe captain+?)
    ALLIANCE_REQUEST("^(?<username>\\S+?) from (?<guild>.+?) is requesting to be allied$"),
    ALLIANCE_SEND("^(?<username>\\S+?) sent (?<guild>.+?) a request to be allied$"),
    ALLIANCE_REJECT("^(?<username>\\S+?) rejected (?<guild>.+?) alliance request$"),

    ALLIANCE_FORM("^(?<guild1>.+?) formed an alliance with (?<guild2>.?)$"),
    ALIANCE_REVOKE("^(?<username>\\S+?) revoked the alliance with (?<guild>.*?)$");
    public final Pattern regex;

    HighRankMessage(String message) {
        this.regex = Pattern.compile(message);
    }

    /**
     * Gets the matcher for the message, expecting it to be the content group from the guild
     * matcher.
     *
     * @param message to check
     * @return the matcher object if it the regex matches in the message, null otherwise
     */
    public Matcher getMatcher(String message) {
        if (message == null) return null;
        Matcher t = regex.matcher(message);
        if (t.find())
            return t;
        return null;
    }
}
