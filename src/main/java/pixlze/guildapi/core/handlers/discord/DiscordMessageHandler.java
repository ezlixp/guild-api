package pixlze.guildapi.core.handlers.discord;

import org.json.JSONObject;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.Managers;
import pixlze.guildapi.core.handlers.Handler;
import pixlze.guildapi.core.handlers.discord.event.S2CDiscordEvents;
import pixlze.guildapi.models.Models;
import pixlze.guildapi.net.SocketIOClient;

public class DiscordMessageHandler extends Handler {

    @Override
    public void init() {
        SocketIOClient socketIOClient = Managers.Net.socket;
        socketIOClient.addDiscordListener("discordMessage", this::onDiscordMessage);
        // TODO handle c2s discord messages too, so all messages can be stored and added to discord screen.
    }

    private void onDiscordMessage(Object[] args) {
        if (args[0] instanceof JSONObject data) {
            try {
                GuildApi.LOGGER.info("received discord {}", data.get("Content").toString());
                if (data.get("Content").toString().isBlank() || Models.DiscordMessage.isBlocked(data.get("Author")
                        .toString().split(" ")[0])) return;
                S2CDiscordEvents.MESSAGE.invoker().interact(data);
            } catch (Exception e) {
                GuildApi.LOGGER.info("discord message error: {} {}", e, e.getMessage());
            }
        } else {
            GuildApi.LOGGER.info("malformed discord message: {}", args);
        }
    }
}
