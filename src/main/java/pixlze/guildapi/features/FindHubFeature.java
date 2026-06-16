package pixlze.guildapi.features;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;
import org.json.JSONObject;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.core.config.Config;
import pixlze.guildapi.core.handlers.discord.event.S2CSocketEvents;
import pixlze.guildapi.features.type.PlayerPositionDTO;
import pixlze.guildapi.features.type.SocketFeature;
import pixlze.guildapi.models.Models;

public class FindHubFeature extends SocketFeature {
    private Vec3d lastSentPos;
    private long lastSentTime;
    private boolean sentPosition = false;

    public FindHubFeature() {
        super("Guild Location Sharing");
    }

    @Override
    public void init() {
        S2CSocketEvents.PLAYER_POSITION.register(this::onPlayerPosition);
        S2CSocketEvents.PLAYER_HIDE.register(this::onPlayerHide);
        ClientTickEvents.END_CLIENT_TICK.register(this::sendLocation);
    }

    @Override
    public void onConfigUpdate(Config<?> config) {

    }

    private void sendLocation(MinecraftClient client) {
        if (client.player == null || client.world == null || !Models.WorldState.onWorld()) {
            if (sentPosition) {
                sentPosition = false;
                Managers.DiscordSocket.emit("playerHide", null);
            }
            return;
        }

        long now = client.world.getTime();
        Vec3d pos = client.player.getEntityPos();

        boolean movedEnough = lastSentPos == null || pos.distanceTo(lastSentPos) > 3.0;

        boolean intervalElapsed = now - lastSentTime > 200;

        if (movedEnough || intervalElapsed) {
            Managers.DiscordSocket.emit("playerPosition", Managers.Json.GSON.toJson(PlayerPositionDTO.fromVec3d(pos)));
            sentPosition = true;
            lastSentPos = pos;
            lastSentTime = now;
        }
    }

    // all json errors should've been handled before posting as longa s we take the correct fields from socket event handler
    private void onPlayerPosition(JSONObject data) {
        if (!Models.WorldState.onWorld()) return;
        try {
            String username = data.getString("username");
            double x = data.getDouble("x");
            double y = data.getDouble("y");
            double z = data.getDouble("z");
            Managers.Waypoint.addWaypoint(username, x, y, z);
        } catch (Exception e) {
            GuildApi.LOGGER.warn("got impossible error: {} {}", e, e.getMessage());
        }
    }

    private void onPlayerHide(String username) {
        Managers.Waypoint.removeWaypoint(username);
    }


}
