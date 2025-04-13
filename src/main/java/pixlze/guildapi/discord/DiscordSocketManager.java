package pixlze.guildapi.discord;

import io.socket.client.IO;
import net.minecraft.text.Text;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.core.features.FeatureState;
import pixlze.guildapi.features.discord.DiscordBridgeFeature;
import pixlze.guildapi.models.worldState.event.WorldStateEvents;
import pixlze.guildapi.models.worldState.type.WorldState;
import pixlze.guildapi.net.type.AbstractSocketManager;
import pixlze.guildapi.utils.ColourUtils;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.type.Prepend;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DiscordSocketManager extends AbstractSocketManager {
    public String guildId;

    public DiscordSocketManager() {
        super(List.of());
    }

    @Override
    protected void tryConnect() {
        if (doConnect()) {
            socket.connect();
            GuildApi.LOGGER.info("discord socket connecting");
        }
    }

    @Override
    protected String getToken() {
        return Managers.Net.guild.getToken(true);
    }

    @Override
    protected void updateSocket() {
        options.extraHeaders.put("from", Collections.singletonList(McUtils.playerName()));
        options.extraHeaders.put("Authorization", Collections.singletonList("bearer " + getToken()));
        socket = IO.socket(URI.create(Managers.Net.guild.getBaseURL() + "discord"), options);
    }

    private void worldStateChanged(WorldState state) {
        if (state == WorldState.WORLD) {
            boolean reload = false;
            if (!Objects.equals(Managers.Net.guild.guildId, guildId)) {
                guildId = Managers.Net.guild.guildId;
                reload = true;
            }
            initSocket(reload);
        } else {
            this.disable();
        }
    }

    @Override
    protected boolean doConnect() {
        if (Managers.Feature.getFeatureState(Managers.Feature.getFeatureInstance(DiscordBridgeFeature.class)) != FeatureState.ENABLED) {
            McUtils.sendLocalMessage(Text.literal("§cDiscord bridging is disabled. Please turn it on in /guildapi config and try again."), Prepend.GUILD.getWithStyle(ColourUtils.RED), true);
            return false;
        }
        if (isDisabled()) {
            McUtils.sendLocalMessage(Text.literal("§cCannot connect to chat server at this time. Please join a world first."), Prepend.GUILD.getWithStyle(ColourUtils.RED), true);
            return false;
        }
        if (socket == null) {
            McUtils.sendLocalMessage(Text.literal("§cCould not find chat server."), Prepend.GUILD.getWithStyle(ColourUtils.RED), true);
            return false;
        }
        return true;
    }

    @Override
    public void init() {
        super.init();
        WorldStateEvents.CHANGE.register(this::worldStateChanged);
    }
}
