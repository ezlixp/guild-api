package pixlze.guildapi.utils;

import pixlze.guildapi.GuildApi;

import java.io.File;

public class FileUtils {
    public static void mkdir(File dir) {
        if (dir.isDirectory()) return;
        if (!dir.mkdirs()) {
            GuildApi.LOGGER.error("couldn't make directory {}", dir);
        }
    }
}
