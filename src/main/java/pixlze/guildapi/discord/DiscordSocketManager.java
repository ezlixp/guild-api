package pixlze.guildapi.discord;

import io.socket.client.Ack;
import io.socket.client.IO;
import net.minecraft.text.Text;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.core.features.FeatureState;
import pixlze.guildapi.features.discord.DiscordBridgeFeature;
import pixlze.guildapi.models.worldState.event.WorldStateEvents;
import pixlze.guildapi.models.worldState.type.WorldState;
import pixlze.guildapi.net.GuildApiClient;
import pixlze.guildapi.net.event.NetEvents;
import pixlze.guildapi.net.type.AbstractSocketManager;
import pixlze.guildapi.net.type.Api;
import pixlze.guildapi.utils.ColourUtils;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.type.Prepend;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DiscordSocketManager extends AbstractSocketManager {
    public String guildId;
    public boolean onWorld = false;

    public DiscordSocketManager() {
        super(List.of());
    }

    private void onApiLoaded(Api api) {
        if (api.getClass().equals(GuildApiClient.class)) initSocket();
    }

    private void onApiUnloaded(Api api) {
        if (api.getClass().equals(GuildApiClient.class)) disable();
    }

    @Override
    protected boolean tryConnect() {
        if (doConnect()) {
            socket.connect();
            GuildApi.LOGGER.info("discord socket connecting");
            return true;
        }
        return false;
    }

    @Override
    protected String getToken() {
        return Managers.Net.guild.getToken(false);
    }

    @Override
    protected void updateSocket() {
        options.extraHeaders.put("from", Collections.singletonList(McUtils.playerName()));
        options.extraHeaders.put("Authorization", Collections.singletonList("bearer " + getToken()));
        socket = IO.socket(URI.create(Managers.Net.guild.getBaseURL() + "discord"), options);
    }

    private void worldStateChanged(WorldState state) {
        if (state == WorldState.WORLD) {
            if (Managers.DiscordSocket.isDisabled())
                onWorld = true;
            else
                Managers.DiscordSocket.emit("sync", (Ack) args -> onWorld = true);
        } else
            onWorld = false;
    }

    public void initSocket() {
        if (Managers.Net.guild.isDisabled()) return;
        boolean reload = false;
        if (!Objects.equals(Managers.Net.guild.guildId, guildId)) {
            Managers.Discord.clearMessages();
            guildId = Managers.Net.guild.guildId;
            reload = true;
        }
        initSocket(reload);
    }

    @Override
    protected boolean doConnect() {
        if (Managers.Feature.getFeatureState(Managers.Feature.getFeatureInstance(DiscordBridgeFeature.class)) != FeatureState.ENABLED) {
            McUtils.sendLocalMessage(Text.literal("§cDiscord bridging is disabled. Please turn it on in /guildapi config and try again."), Prepend.GUILD.getWithStyle(ColourUtils.RED), true);
            return false;
        }
        if (socket == null) {
            McUtils.sendLocalMessage(Text.literal("§cCould not find chat server."), Prepend.GUILD.getWithStyle(ColourUtils.RED), true);
            return false;
        }
        return true;
    }

    @Override
    protected String disabledMessage() {
        return "§cCannot connect to chat server at this time. Please enable discord bridging and try again.";
    }

    @Override
    public void init() {
        super.init();
        NetEvents.LOADED.register(this::onApiLoaded);
        NetEvents.DISABLED.register(this::onApiUnloaded);
        WorldStateEvents.CHANGE.register(this::worldStateChanged);
    }
}
