package pixlze.guildapi.features;

import net.minecraft.text.Text;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.Managers;
import pixlze.guildapi.core.config.Config;
import pixlze.guildapi.core.features.Feature;
import pixlze.guildapi.net.GuildApiClient;
import pixlze.guildapi.net.event.NetEvents;
import pixlze.guildapi.net.type.Api;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.NetUtils;
import pixlze.guildapi.utils.type.Prepend;

public class AutoUpdateFeature extends Feature {
    private boolean completed = false;
    private boolean needUpdate = false;
    private String modDownloadURL;

    @Override
    public void init() {
        NetEvents.LOADED.register(this::onApiLoaded);
    }

    @Override
    public void onConfigUpdate(Config<?> config) {

    }

    private void onApiLoaded(Api loaded) {
        if (!completed && loaded.getClass().equals(GuildApiClient.class)) {
            Managers.Net.guild.get("mod/update").whenCompleteAsync((res, err) -> {
                try {
                    NetUtils.applyDefaultCallback(res, err, (resOK) -> {
                        GuildApi.LOGGER.info("auto update result: {}", resOK);
                        String latestVersion = resOK.getAsJsonObject().get("versionNumber").getAsString();
                        if (!GuildApi.MOD_VERSION.equals(latestVersion)) {
                            GuildApi.LOGGER.info("outdated version: {}", GuildApi.MOD_VERSION);
                            McUtils.sendLocalMessage(Text.literal("§a[Guild Api] You are running build v" + GuildApi.MOD_VERSION + ", but the latest build is v" + latestVersion + "." +
                                    " " +
                                    "Please consider updating through modrinth."), Prepend.EMPTY.get(), false);
                        }
                    }, NetUtils.defaultFailed("mod update check", false));
                } catch (Exception e) {
                    NetUtils.defaultException("auto update", e);
                }
            });
            completed = true;
        }
    }


}
