package pixlze.guildapi.features.discord;

import com.google.gson.JsonElement;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.text.Text;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.components.Models;
import pixlze.guildapi.features.type.ListFeature;
import pixlze.guildapi.net.GuildApiClient;
import pixlze.guildapi.net.event.NetEvents;
import pixlze.guildapi.net.type.Api;
import pixlze.guildapi.utils.JsonUtils;
import pixlze.guildapi.utils.McUtils;

import java.util.List;

public class DiscordBlockFeature extends ListFeature {
    private final GuildApiClient guildApiClient = Managers.Net.guild;

    public DiscordBlockFeature() {
        super("block", "user/blocked/" + McUtils.playerUUID(), (listItem) -> Text.literal(listItem.getAsString()));
    }

    @Override
    public void init() {
        super.registerCommands(List.of(
                ClientCommandManager.literal("add").then(ClientCommandManager.argument("toBlock", StringArgumentType.word()).executes((context) -> {
                    String toBlock = StringArgumentType.getString(context, "toBlock");
                    guildApiClient.post("user/blocked/" + McUtils.playerUUID(), JsonUtils.toJsonObject("{toBlock:\"" + toBlock + "\"}"), true);
                    Models.DiscordMessage.block(toBlock);
                    return Command.SINGLE_SUCCESS;
                })))
        );
        NetEvents.LOADED.register(this::onApiLoaded);
    }

    private void onApiLoaded(Api api) {
        if (api.getClass().equals(GuildApiClient.class)) {
            guildApiClient.get("user/blocked/" + McUtils.playerUUID(), false).whenCompleteAsync((res, error) -> {
                if (error != null) {
                    GuildApi.LOGGER.error("get blocked error: {} {}", error.getMessage(), error.getMessage());
                    return;
                }
                GuildApi.LOGGER.info("{} blocked peoploe:", res);
                List<JsonElement> blocked = res.getAsJsonArray().asList();
                for (JsonElement block : blocked) {
                    GuildApi.LOGGER.info("blocking: {}", block.getAsString());
                    Models.DiscordMessage.block(block.getAsString());
                }
            });
        }
    }
}
