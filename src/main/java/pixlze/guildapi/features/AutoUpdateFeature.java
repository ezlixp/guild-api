package pixlze.guildapi.features;

import com.google.gson.JsonElement;
import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.net.GuildApiClient;
import pixlze.guildapi.net.event.NetEvents;
import pixlze.guildapi.net.type.Api;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.type.Prepend;

import java.util.concurrent.CompletableFuture;

public class AutoUpdateFeature extends Feature {
    private boolean completed = false;
    private boolean needUpdate = false;
    private String modDownloadURL;

    @Override
    public void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(GuildApi.BASE_COMMAND.then(ClientCommandManager.literal("update").executes((context) -> {
                GuildApi.LOGGER.info("guild update");
                // do some md5 verification stuff on downloaded file
                return Command.SINGLE_SUCCESS;
            })));
        });
        NetEvents.LOADED.register(this::onApiLoaded);
    }

    private void onApiLoaded(Api loaded) {
        if (!completed && loaded.getClass().equals(GuildApiClient.class)) {
            CompletableFuture<JsonElement> response = Managers.Net.guild.get("update");
            response.whenCompleteAsync((res, err) -> {
                if (err != null) {
                    GuildApi.LOGGER.error("get update error: {} {}", err, err.getMessage());
                    return;
                }
                GuildApi.LOGGER.info("auto update result: {}", res);
                try {
                    String latestVersion = res.getAsJsonObject().get("versionNumber").getAsString();
                    if (!GuildApi.MOD_VERSION.equals(latestVersion)) {
                        GuildApi.LOGGER.info("outdated version: {}", GuildApi.MOD_VERSION);
                        McUtils.sendLocalMessage(Text.literal("§a[Guild Api] You are running build v" + GuildApi.MOD_VERSION + ", but the latest build is v" + latestVersion + "." +
                                " " +
                                "Please consider updating through modrinth."), Prepend.EMPTY.get(), false);
                    }
                } catch (Exception e) {
                    GuildApi.LOGGER.error("auto update error: {} {}", e, e.getMessage());
                }
            });
            completed = true;
        }
    }


}
