package pixlze.guildapi.features.discord;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.components.Feature;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.components.Models;
import pixlze.guildapi.net.GuildApiClient;
import pixlze.guildapi.net.event.NetEvents;
import pixlze.guildapi.net.type.Api;
import pixlze.guildapi.utils.JsonUtils;
import pixlze.guildapi.utils.McUtils;

public class DiscordBlockFeature extends Feature {
    private final GuildApiClient guildApiClient = Managers.Net.guild;

    @Override
    public void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(GuildApi.BASE_COMMAND.then(ClientCommandManager.literal("block")
                    .then(ClientCommandManager.argument("toBlock", StringArgumentType.word()).executes((context) -> {
                        String toBlock = StringArgumentType.getString(context, "toBlock");
                        guildApiClient.post("user/blocked/" + McUtils.player()
                                .getUuidAsString(), JsonUtils.toJsonObject("{toBlock:\"" + toBlock + "\"}"), true);
                        Models.DiscordMessage.block(toBlock);
                        return Command.SINGLE_SUCCESS;
                    }))));
        });
        NetEvents.LOADED.register(this::onApiLoaded);
    }

    private void onApiLoaded(Api api) {
        if (api.getClass().equals(GuildApiClient.class)) {
            guildApiClient.get("user/blocked/" + McUtils.player().getUuidAsString()).whenCompleteAsync((res, error) -> {
                if (error != null) {
                    GuildApi.LOGGER.error("get blocked error: {} {}", error.getMessage(), error.getMessage());
                }
                GuildApi.LOGGER.info("{} blocked peoploe:", res);
            });
        }
    }
}
