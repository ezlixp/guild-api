package pixlze.pixutils.handlers.chat;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.text.Text;
import pixlze.pixutils.PixUtils;

public final class ChatHandler {
    private void onWynnMessage(Text message, boolean overlay) {
        // check if on wynn.
        if (!overlay) PixUtils.LOGGER.info("chat message");
    }

    public void init() {
        PixUtils.LOGGER.info("handler");
        ClientReceiveMessageEvents.GAME.register(this::onWynnMessage);
    }
}
