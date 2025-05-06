package pixlze.guildapi.core.handlers.discord;

import org.json.JSONObject;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.components.Handler;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.core.handlers.discord.event.S2CSocketEvents;
import pixlze.guildapi.models.Models;

public class SocketEventHandler extends Handler {

    @Override
    public void init() {
        Managers.DiscordSocket.saveListener("discordMessage", this::onDiscordMessage);
        Managers.DiscordSocket.saveListener("wynnMirror", this::onWynnMirror);
    }

    private void onDiscordMessage(Object[] args) {
        if (args[0] instanceof JSONObject data) {
            try {
                GuildApi.LOGGER.info("received discord {}", data.get("Content").toString());
                if (data.get("Content").toString().isBlank() || Models.DiscordMessage.isBlocked(data.get("Author")
                        .toString().split(" ")[0])) return;
                S2CSocketEvents.DISCORD_MESSAGE.invoker().interact(data);
            } catch (Exception e) {
                GuildApi.LOGGER.info("discord message error: {} {}", e, e.getMessage());
            }
        } else {
            GuildApi.LOGGER.info("malformed discord message: {}", args);
        }
    }

    private void onWynnMirror(Object[] args) {
        if (args[0] instanceof String data) {
            GuildApi.LOGGER.info("received mirror {}", data);
            S2CSocketEvents.WYNN_MIRROR.invoker().interact(data);
        } else {
            GuildApi.LOGGER.info("malformed wynn mirror message: {}", args);
        }
    }
}
