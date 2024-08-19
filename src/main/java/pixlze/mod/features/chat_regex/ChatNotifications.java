package pixlze.mod.features.chat_regex;

import oshi.util.tuples.Pair;
import pixlze.mod.config.PixUtilsConfig;
import pixlze.mod.config.types.SubConfig;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class ChatNotifications {
    static public SubConfig<Pair<Pattern, String>> config;

    public static void initialize() {
        EditNotificationsScreen editNotificationsScreen = new EditNotificationsScreen();
        config = PixUtilsConfig.registerSubConfig("Notifications", "Edit Notifications", editNotificationsScreen, new ArrayList<Pair<Pattern, String>>());
    }
}
