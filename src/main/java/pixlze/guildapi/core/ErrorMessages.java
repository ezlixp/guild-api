package pixlze.guildapi.core;

public class ErrorMessages {
    // authentication
    public static final String API_DISABLED_ERROR = "API is disabled";
    public static final String INVALID_TOKEN = "Invalid token provided.";
    public static final String INVALID_REFRESH = "Invalid refresh token.";
    public static final String NO_TOKEN = "No token provided.";
    public static final String NO_REFRESH = "No refresh token provided.";
    public static final String UNPRIVILEGED_TOKEN = "Token does not have access to selected resource.";
    public static final String BANNED = "You are banned.";

    // blocked list
    public static final String FULL_BLOCKED_LIST = "The blocked user list is currently full.";
    public static final String ALREADY_IN_BLOCKED_LIST = "The provided user is already present on the blocked list.";
    public static final String NOT_IN_BLOCKED_LIST = "The provided user is not present on the blocked list.";

    // raid list
    public static final String NOT_IN_RAID_LIST = "The provided username could not be found on the rewards list.";

    // tome list
    public static final String TOME_DUPLICATE = "The provided username is already in the tome list.";
    public static final String TOME_NOT_FOUND = "The provided username could not be found on the tome list.";
}
