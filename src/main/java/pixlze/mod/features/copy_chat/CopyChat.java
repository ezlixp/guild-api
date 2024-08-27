package pixlze.mod.features.copy_chat;

import pixlze.mod.PixUtils;
import pixlze.mod.config.PixUtilsConfig;
import pixlze.mod.config.types.Toggle;

public class CopyChat {
    static final String FEATURE_ID = "copy_chat";
    static public Toggle config;

    // issue with new messages coming in while screen open.
    public static void initialize() {
        boolean prev = false;
        if (PixUtilsConfig.configObject != null) {
            try {
                prev = PixUtilsConfig.configObject.get(FEATURE_ID).getAsBoolean();
            } catch (Exception e) {
                PixUtils.LOGGER.error(e.getMessage());
            }
        }
        config = PixUtilsConfig.registerToggle(FEATURE_ID, prev);
    }
}
