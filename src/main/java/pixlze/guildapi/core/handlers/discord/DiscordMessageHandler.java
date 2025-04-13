package pixlze.guildapi.core.handlers.discord;

import org.json.JSONObject;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.components.Handler;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.core.handlers.discord.event.S2CDiscordEvents;
import pixlze.guildapi.models.Models;

public class DiscordMessageHandler extends Handler {

    @Override
    public void init() {
        Managers.DiscordSocket.saveListener("discordMessage", this::onDiscordMessage);
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
