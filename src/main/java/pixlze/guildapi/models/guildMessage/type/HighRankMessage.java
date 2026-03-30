package pixlze.guildapi.models.guildMessage.type;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum HighRankMessage {
    // Eco
    TERRITORY_BONUS_MODIFY("^§.(?<username>.+?)§. set §.(?<bonus>.+?)§. to level §.(?<level>.+?)§. on §.(?<territory>.*)$"),
    TERRITORY_BONUS_REMOVE("^§.(?<username>.+?)§. removed §.(?<changed>.+?)§. from §.(?<territory>.*)$"),
    TERRITORY_BONUS_MASS_MODIFY("^§.(?<username>.+?)§. changed §.(?<amount>\\d+) (?<changed>\\w+)§. on §3(?<territory>.*)$"),
    TERRITORY_LOADOUT_APPLY("^§.(?<username>.+?)§. applied the loadout §(?<loadout>.+?)§. on §.(?<territory>.*)$"),
    TERRITORY_RESOURCE_WARNING("^Territory §.(?<territory>.+?)§. is \\w+ more resources than it can store!$"),
    TERRITORY_RESOURCE_STABILISE("^Territory §.(?<territory>.+?)§. production has stabilised$"),
    // Guild bank
    GUILD_BANK("^§.(?<username>.+?)§. (?<action>\\w+) §.(?<item>.+?)§. (?:to|from) the Guild Bank \\(§.High Ranked§.\\)$"),
    // Guild tome found
    TOME_FOUND("^§.A Guild Tome§. has been found and added to the Guild Rewards$"),

    // Unsure if these are accurate or if available to all (maybe captain+?)
    ALLIANCE_REQUEST("^(?<username>.+?) from (?<guild>.+?) is requesting to be allied$"),
    ALLIANCE_SEND("^(?<username>.+?) sent (?<guild>.+?) a request to be allied$"),
    ALLIANCE_REJECT("^(?<username>.+?) rejected (?<guild>.+?) alliance request$"),

    ALLIANCE_FORM("^(?<guild1>.+?) formed an alliance with (?<guild2>.?)$"),
    ALIANCE_REVOKE("^(?<username>.+?) revoked the alliance with (?<guild>.*?)$");
    public final Pattern regex;

    private HighRankMessage(String message) {
        this.regex = Pattern.compile(message);
    }

    public Matcher getMatcher(String message) {
        Matcher t = regex.matcher(message);
        if (t.find())
            return t;
        return null;
    }
}
