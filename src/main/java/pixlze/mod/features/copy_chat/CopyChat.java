package pixlze.mod.features.copy_chat;

import pixlze.mod.config.PixUtilsConfig;
import pixlze.mod.config.types.Toggle;

public class CopyChat {
    static public Toggle config;

    public static void initialize() {
        config = PixUtilsConfig.registerToggle("Ctrl-click to copy chat");
    }
}
