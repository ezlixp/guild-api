package pixlze.guildapi.models.guildMessage.type;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum GuildMessage {
    // Basic guild chat message
    BASIC("^(?<pill>.*)§[38](?<header>.+?)(§[38])?:§[b8] (?<content>.*)$"),

    // Guild raid finished
    RAID_FINISH("^§[e8](?<player1>.*?)§[b8], §[e8](?<player2>.*?)§[b8], §[e8](?<player3>.*?)§[b8], and §[e8](?<player4>.*?)§[b8] finished §[38](?<raid>.*?)§[b8].*$"),

    // Giving out resources
    ASPECT_GIVE("^§.(?<giver>.*?)(§.)? rewarded §.an Aspect§. to §.(?<receiver>.*?)(§.)?$"),
    TOME_GIVE("^§.(?<giver>.*?)(§.)? rewarded §.a Guild Tome§. to §.(?<receiver>.*?)(§.)?$"),
    EMERALD_GIVE("^§.(?<giver>.*?)(§.)? rewarded §.1024 Emeralds§. to §.(?<receiver>.*?)(§.)?$"),

    // Guild bank
    GUILD_BANK("^§.(?<username>.+?)§. (?<action>\\w+) §.(?<item>.+?)§. (?:to|from) the Guild Bank \\(§.Everyone§.\\)"),

    // Weekly objective
    WEEKLY_OBJECTIVE("^(?<username>.+?) has finished their weekly objective\\.$"),
    WEEKLY_OBJECTIVE_REMINDER("^Only (?<time>.+?) left to complete the Weekly Guild Objectives!$"),

    // Guild member management
    MEMBER_INVITE("^(?<recruiter>.+?) has invited (?<recruit>.+?) to the guild$"),
    MEMBER_UNINVITE("^(?<recruiter>.+?) has uninvited (?<recruit>.+?) from the guild$"),
    MEMBER_JOIN("^(?<recruit>.+?) has joined the guild, say hello!$"),
    MEMBER_LEAVE("^(?<username>.+?) has left the guild$"),
    MEMBER_KICK("^(?<kicker>.+?) has kicked (?<kicked>.+?) from the guild$"),
    MEMBER_RANK("^(?<setter>.+?) has set (?<set>.+?) guild rank from §.(?<original>\\w+)§. to §.(?<new>\\w+)$"),

    // War
    WAR_COUNTDOWN("^The war for (?<territory>.+?) will start in .*$"),
    WAR_LOSE("^Your guild has lost the war for .*$"),
    WAR_BEGIN("^The battle has begun!$"),
    WAR_WIN("^You have taken control of .*$"),
    WAR_DEFEND_WIN("^\\[\\w+\\] has lost the war!.*$"),
    WAR_DEFEND_LOSE("^\\[\\w+\\] has taken control of .*$"),

    // Guild season
    GUILD_SEASON_END("^The current guild season will end in .*$"),
    GUILD_SEASON_REWARD_REMINDER("^The last standing territories you control once it ends will grant you 2048² each!$"),

    // Misc
    MEMBER_BOOST("^§.(?<username>.+?) has started boosting the guild$");


    public final Pattern regex;

    GuildMessage(String pattern) {
        regex = Pattern.compile(pattern);
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
        else return null;
    }
}
