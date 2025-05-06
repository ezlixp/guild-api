package pixlze.guildapi.core.handlers.connection;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.handlers.connection.event.WynncraftConnectionEvents;

import java.net.InetSocketAddress;
import java.util.regex.Pattern;

public class ConnectionHandler {
    private static final Pattern WYNNCRAFT_SERVER_PATTERN =
            Pattern.compile("^(?:(.*)\\.)?wynncraft\\.(?:com|net|org)\\.?$");
    private boolean isConnected = false;

    public boolean onWynncraft() {
        return isConnected;
    }

    public void init() {
        ClientPlayConnectionEvents.JOIN.register(this::onConnected);
        ClientPlayConnectionEvents.DISCONNECT.register(this::onDisconnected);
    }

    public void onConnected(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
        if (handler.getConnection().getAddress() instanceof InetSocketAddress address) {
            GuildApi.LOGGER.info("ip: {}", address.getHostName());
            if (!isConnected && WYNNCRAFT_SERVER_PATTERN.matcher(address.getHostName()).matches()) {
                connect();
            } else if (WYNNCRAFT_SERVER_PATTERN.matcher(address.getHostName()).matches()) {
                GuildApi.LOGGER.info("server change");
                WynncraftConnectionEvents.CHANGE.invoker().interact();
            }
        }
        if (GuildApi.isDevelopment())
            connect();
    }

    private void onDisconnected(ClientPlayNetworkHandler clientPlayNetworkHandler, MinecraftClient minecraftClient) {
        if (isConnected)
            disconnect();
    }

    private void connect() {
        isConnected = true;
        GuildApi.LOGGER.info("on wynn");
        WynncraftConnectionEvents.JOIN.invoker().interact();
    }

    private void disconnect() {
        isConnected = false;
        GuildApi.LOGGER.info("off wynn");
        WynncraftConnectionEvents.LEAVE.invoker().interact();
    }

}
