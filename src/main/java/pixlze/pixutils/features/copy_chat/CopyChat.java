package pixlze.pixutils.features.copy_chat;

import pixlze.pixutils.PixUtils;
import pixlze.pixutils.components.Managers;
import pixlze.pixutils.config.types.Toggle;

public class CopyChat {
    static final String FEATURE_ID = "copy_chat";
    static final String FEATURE_NAME = "Ctrl-click to copy chat";
    static public Toggle config;

    // issue with new messages coming in while screen open.
    public static void initialize() {
        boolean prev = false;
        if (Managers.Config.configObject != null) {
            try {
                prev = Managers.Config.configObject.get(FEATURE_ID).getAsBoolean();
            } catch (Exception e) {
                PixUtils.LOGGER.error(e.getMessage());
            }
        }
        config = Managers.Config.registerToggle(FEATURE_NAME, FEATURE_ID, prev);
    }
}
