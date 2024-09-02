package pixlze.guildapi.mod;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import pixlze.guildapi.GuildApi;

import java.util.Objects;
import java.util.regex.Pattern;

public class ConnectionManager {
    private static final Pattern WYNNCRAFT_SERVER_PATTERN =
            Pattern.compile("^(?:(.*)\\.)?wynncraft\\.(?:com|net|org)$");
    private boolean isConnected = false;

    public boolean onWynncraft() {
        return isConnected;
    }

    private void onDisconnected(ClientPlayNetworkHandler clientPlayNetworkHandler, MinecraftClient minecraftClient) {
        disconnect();
    }

    public void onConnected(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
        if (client.player != null) {
            String host = Objects.requireNonNull(client.player.getServer()).toString();
            if (WYNNCRAFT_SERVER_PATTERN.matcher(host).matches()) {
                connect();
            }
        }
    }

    private void connect() {
        isConnected = true;
        GuildApi.LOGGER.info("on wynn");
        // post wynncraft connected event
    }

    private void disconnect() {
        isConnected = false;
        GuildApi.LOGGER.info("off wynn");
        // post wynncraft disconnected event
    }

    public void init() {
        ClientPlayConnectionEvents.JOIN.register(this::onConnected);
        ClientPlayConnectionEvents.DISCONNECT.register(this::onDisconnected);
    }

}
