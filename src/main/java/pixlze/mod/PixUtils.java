package pixlze.mod;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pixlze.mod.features.copy_chat.CopyChat;

public class PixUtils implements ModInitializer {
    public static final String MOD_ID = "pixutils";
    public static final Logger LOGGER = LoggerFactory.getLogger("pixutils");

    @Override
    public void onInitialize() {
        System.setProperty("java.awt.headless", "false");
        CopyChat.initialize();
    }
}