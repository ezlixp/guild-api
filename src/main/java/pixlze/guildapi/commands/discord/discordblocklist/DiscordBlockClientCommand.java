package pixlze.guildapi.commands.discord.discordblocklist;

import com.google.gson.JsonElement;
import net.minecraft.text.Text;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.commands.type.ListClientCommand;
import pixlze.guildapi.core.commands.ClientCommand;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.models.Models;
import pixlze.guildapi.net.GuildApiClient;
import pixlze.guildapi.net.event.NetEvents;
import pixlze.guildapi.net.type.Api;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.NetUtils;
import pixlze.guildapi.utils.type.Prepend;

import java.util.List;

// TODO migrate this to feature command cuz its a feature tbh
public class DiscordBlockClientCommand extends ListClientCommand {
    private static final String ENDPOINT = "user/blocked/";
    private final GuildApiClient guildApiClient = Managers.Net.guild;

    public DiscordBlockClientCommand() {
        super("block", ENDPOINT, (listItem) -> {
            Models.DiscordMessage.block(listItem.getAsString());
            return Text.literal(listItem.getAsString());
        });
        NetEvents.LOADED.register(this::onApiLoaded);
    }

    @Override
    protected List<String> getAliases() {
        return List.of("bl");
    }

    @Override
    protected List<ClientCommand> getSubCommands() {
        return List.of(new AddSubCommand(), new RemoveSubCommand());
    }

    private void onApiLoaded(Api api) {
        if (api.getClass().equals(GuildApiClient.class)) {
            setExtra(McUtils.playerUUID());

            guildApiClient.get(ENDPOINT + McUtils.playerUUID(), false).whenCompleteAsync((res, error) -> {
                try {
                    NetUtils.applyDefaultCallback(res, error, (resOK) -> {
                        List<JsonElement> blocked = resOK.getAsJsonArray().asList();
                        for (JsonElement block : blocked) {
                            GuildApi.LOGGER.info("blocking: {}", block.getAsString());
                            Models.DiscordMessage.block(block.getAsString());
                        }
                    }, NetUtils.defaultFailed("Blocked users fetch", true));
                } catch (Exception e) {
                    McUtils.sendLocalMessage(Text.literal("§cSomething went wrong. Check logs for more details."), Prepend.DEFAULT.get(), false);
                    GuildApi.LOGGER.error("blocked users fetch error: {} {}", e, e.getMessage());
                }
            });
        }
    }
}
