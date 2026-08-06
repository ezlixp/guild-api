package pixlze.guildapi.features;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;
import org.json.JSONObject;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.core.config.Config;
import pixlze.guildapi.core.config.Configurable;
import pixlze.guildapi.core.features.FeatureState;
import pixlze.guildapi.core.handlers.discord.event.S2CSocketEvents;
import pixlze.guildapi.features.type.PlayerPositionDTO;
import pixlze.guildapi.features.type.SocketFeature;
import pixlze.guildapi.models.Models;
import pixlze.guildapi.models.worldState.event.WorldStateEvents;
import pixlze.guildapi.models.worldState.type.WorldState;
import pixlze.guildapi.screens.findhub.WaypointToggleScreen;
import pixlze.guildapi.utils.McUtils;

import java.util.HashSet;
import java.util.Locale;

public class FindHubFeature extends SocketFeature {
    private Vec3d lastSentPos;
    private long lastSentTime;

    @Configurable
    public final Config<Boolean> viewOthers = new Config<>(false);

    @Configurable
    public final Config<HashSet<String>> viewBan = new Config<>(new HashSet<>(), WaypointToggleScreen.class);

    public FindHubFeature() {
        super("Guild Location Sharing");
    }

    @Override
    public void init() {
        S2CSocketEvents.PLAYER_POSITION.register(this::onPlayerPosition);
        S2CSocketEvents.PLAYER_HIDE.register(this::onPlayerHide);
        ClientTickEvents.END_CLIENT_TICK.register(this::sendLocation);
        WorldStateEvents.CHANGE.register(this::onWorldState);
    }

    @Override
    public void onConfigUpdate(Config<?> config) {
        if (config.getName().equals("viewOthers")) {
            boolean value = (boolean) config.getValue();
            if (value) Managers.Waypoint.setAllActive(this::seeWaypoint);
            else Managers.Waypoint.setAllActive((username) -> false);
        } else if (config.getName().equals("viewBan")) {
            Managers.Waypoint.setAllActive((username) -> viewOthers.getValue() && seeWaypoint(username));
        }
    }

    @Override
    public void onEnabled() {
        Managers.DiscordSocket.emit("requestAllPositions", null);
        sendLocation(McUtils.mc());
    }

    @Override
    public void onDisabled() {
        Managers.DiscordSocket.emit("playerHide", null);
        Managers.Waypoint.clearWaypoints();
        lastSentPos = null;
        lastSentTime = 0;
    }

    public void onWorldState(WorldState worldState) {
        if (worldState != WorldState.WORLD) Managers.DiscordSocket.emit("playerHide", null);
    }

    private void sendLocation(MinecraftClient client) {
        if (Managers.Feature.getFeatureState(this) != FeatureState.ENABLED || client.player == null || client.world == null || !Models.WorldState.onWorld())
            return;

        long now = client.world.getTime();
        Vec3d pos = client.player.getEntityPos();

        boolean movedEnough = lastSentPos == null || pos.distanceTo(lastSentPos) > 1.0;

        boolean intervalElapsed = now - lastSentTime > 20;

        if (movedEnough || intervalElapsed) {
            Managers.DiscordSocket.emit("playerPosition", Managers.Json.GSON.toJson(PlayerPositionDTO.fromVec3d(pos)));
            lastSentPos = pos;
            lastSentTime = now;
        }
    }

    private boolean seeWaypoint(String username) {
        return !viewBan.getValue().contains(username.toLowerCase(Locale.ROOT));
    }

    // all json errors should've been handled before posting as longa s we take the correct fields from socket event handler
    private void onPlayerPosition(JSONObject data) {
        if (!Models.WorldState.onWorld() || Managers.Feature.getFeatureState(this) != FeatureState.ENABLED) return;
        try {
            String username = data.getString("username");
            double x = data.getDouble("x");
            double y = data.getDouble("y");
            double z = data.getDouble("z");
            Managers.Waypoint.addWaypoint(username, x, y, z, viewOthers.getValue() && seeWaypoint(username));
        } catch (Exception e) {
            GuildApi.LOGGER.warn("got impossible error: {} {}", e, e.getMessage());
        }
    }

    private void onPlayerHide(String username) {
        Managers.Waypoint.removeWaypoint(username);
    }
}
