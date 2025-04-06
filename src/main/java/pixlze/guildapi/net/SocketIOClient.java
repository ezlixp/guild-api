package pixlze.guildapi.net;

import com.mojang.brigadier.Command;
import io.socket.client.IO;
import io.socket.client.Socket;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import org.json.JSONObject;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.Managers;
import pixlze.guildapi.core.features.FeatureState;
import pixlze.guildapi.features.discord.DiscordBridgeFeature;
import pixlze.guildapi.models.Models;
import pixlze.guildapi.models.worldState.event.WorldStateEvents;
import pixlze.guildapi.models.worldState.type.WorldState;
import pixlze.guildapi.net.type.Api;
import pixlze.guildapi.utils.ColourUtils;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.type.Prepend;

import java.net.URI;
import java.util.*;
import java.util.function.Consumer;


public class SocketIOClient extends Api {
    private static SocketIOClient instance;
    private final ArrayList<Pair<String, Consumer<Object[]>>> listeners = new ArrayList<>();
    public Socket discordSocket;
    private boolean firstConnect = true;
    private int connectAttempt = 0;
    private GuildApiClient guild;
    public String guildId;
    private final IO.Options options = IO.Options.builder()
            .setExtraHeaders(new HashMap<>(Map.of("user" +
                    "-agent", Collections.singletonList(GuildApi.MOD_ID + "/" + GuildApi.MOD_VERSION))))
            .setTimeout(60000)
            .setReconnection(false)
            .build();

    // TODO if add multiple sockets, create wrapper class for each socket with add listeners, etc.
    public SocketIOClient() {
        super("socket", List.of(GuildApiClient.class));
        instance = this;
    }

    public void emit(Socket socket, String event, Object data) {
        if (socket != null && socket.connected()) {
            GuildApi.LOGGER.info("emitting, {}", data);
            socket.emit(event, data);
        } else {
            GuildApi.LOGGER.warn("skipped event because of missing or inactive socket");
        }
    }

    public static SocketIOClient getInstance() {
        return instance;
    }

    @Override
    public void ready() {
        if (Managers.Feature.getFeatureState(Managers.Feature.getFeatureInstance(DiscordBridgeFeature.class)) != FeatureState.ENABLED)
            return;
        guild = Managers.Net.guild;

        options.extraHeaders.put("from", Collections.singletonList(McUtils.playerName()));
        boolean reloadSocket = false;
        if (!guild.guildId.equals(guildId)) {
            guildId = guild.guildId;
            reloadSocket = true;
        }
        initSocket(reloadSocket);
        super.enable();
    }

    @Override
    public void unready() {
        super.unready();
        resetConnection();
    }

    @Override
    public void disable() {
        super.disable();
        resetConnection();
    }

    private void resetConnection() {
        if (discordSocket != null)
            discordSocket.disconnect();
        options.extraHeaders.clear();
        options.extraHeaders.put("user-agent", Collections.singletonList(GuildApi.MOD_ID + "/" + GuildApi.MOD_VERSION));
        firstConnect = true;
        connectAttempt = 0;
    }

    private void initSocket(boolean reloadSocket) {
        if (reloadSocket) {
            firstConnect = true;
            options.extraHeaders.put("Authorization", Collections.singletonList("bearer " + guild.getToken(true)));
            discordSocket = IO.socket(URI.create(guild.getBaseURL() + "discord"), options);
            for (Pair<String, Consumer<Object[]>> listener : listeners) {
                registerDiscordListener(listener.getLeft(), listener.getRight());
            }

            registerDiscordListener(Socket.EVENT_DISCONNECT, (reason) -> {
                GuildApi.LOGGER.info("{} disconnected", reason);
                McUtils.sendLocalMessage(Text.literal("§cDisconnected from chat server."),
                        Prepend.GUILD.getWithStyle(Style.EMPTY.withColor(Formatting.RED)), true);
                if (reason[0].equals("io client disconnect") || reason[0].equals("forced close")) {
                    GuildApi.LOGGER.info("{} skip", reason);
                    return;
                }
                connectAttempt = 0;

                try {
                    Thread.sleep(1000);
                    discordSocket.connect();
                } catch (InterruptedException e) {
                    GuildApi.LOGGER.error("thread sleep error: {} {}", e, e.getMessage());
                }
            });

            registerDiscordListener(Socket.EVENT_CONNECT_ERROR, (err) -> {
                if (connectAttempt % 5 == 0) {
                    if (firstConnect) McUtils.sendLocalMessage(Text.literal("§eConnecting to chat server..."),
                            Prepend.GUILD.getWithStyle(ColourUtils.YELLOW), true);
                    else McUtils.sendLocalMessage(Text.literal("§eReconnecting..."),
                            Prepend.GUILD.getWithStyle(ColourUtils.YELLOW), true);
                }
                GuildApi.LOGGER.info("{} reconnect error", err);

                if (err[0] instanceof JSONObject error) {
                    try {
                        String message = error.getString("message");
                        if (message.equals("Invalid token provided") || message.equals("No token provided"))
                            options.extraHeaders.put("Authorization", Collections.singletonList("bearer " + guild.getToken(true)));
                    } catch (Exception e) {
                        GuildApi.LOGGER.error("connect error: {} {}", e, e.getMessage());
                    }
                }
                try {
                    Thread.sleep(1000);
                    if (++connectAttempt < 10) {
                        discordSocket.disconnect();
                        discordSocket.connect();
                    } else
                        McUtils.sendLocalMessage(Text.literal("§cCould not connect to chat server. Type /reconnect to try again."),
                                Prepend.GUILD.getWithStyle(ColourUtils.RED), true);
                } catch (Exception e) {
                    GuildApi.LOGGER.error("reconnect discord error: {} {}", e, e.getMessage());
                }
            });

            registerDiscordListener(Socket.EVENT_CONNECT, (args) -> {
                McUtils.sendLocalMessage(Text.literal("§aSuccessfully connected to chat server."),
                        Prepend.GUILD.getWithStyle(Style.EMPTY.withColor(Formatting.GREEN)), true);
                firstConnect = false;
            });
        }
        if (GuildApi.isDevelopment() || Models.WorldState.onWorld()) {
            connectDiscord();
        }
        WorldStateEvents.CHANGE.register(this::worldStateChanged);
    }

    public void connectDiscord() {
        if (Managers.Feature.getFeatureState(Managers.Feature.getFeatureInstance(DiscordBridgeFeature.class)) == FeatureState.ENABLED) {
            discordSocket.connect();
            GuildApi.LOGGER.info("sockets connecting");
        }
    }

    public void addDiscordListener(String name, Consumer<Object[]> listener) {
        listeners.add(new Pair<>(name, listener));
        registerDiscordListener(name, listener);
    }

    public void registerDiscordListener(String name, Consumer<Object[]> listener) {
        if (discordSocket != null)
            discordSocket.on(name, listener::accept);
    }

    private void worldStateChanged(WorldState state) {
        if (state == WorldState.WORLD) {
            this.enable();
            if (!discordSocket.connected()) {
                connectDiscord();
            }
        } else {
            this.disable();
            connectAttempt = 999;
            if (discordSocket.connected()) {
                discordSocket.disconnect();
                GuildApi.LOGGER.info("discord socket off");
            }
        }
    }

    @Override
    public void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("reconnect").executes((context) -> {
                if (Managers.Feature.getFeatureState(Managers.Feature.getFeatureInstance(DiscordBridgeFeature.class)) != FeatureState.ENABLED) {
                    McUtils.sendLocalMessage(Text.literal("§cDiscord bridging is disabled. Please turn it on in /guildapi config and try again."), Prepend.GUILD.getWithStyle(ColourUtils.RED), true);
                    return 0;
                }
                if (isDisabled()) {
                    McUtils.sendLocalMessage(Text.literal("§cCannot connect to chat server at this time. Please join a world first."), Prepend.GUILD.getWithStyle(ColourUtils.RED), true);
                    return 0;
                }
                if (discordSocket == null) {
                    McUtils.sendLocalMessage(Text.literal("§cCould not find chat server."), Prepend.GUILD.getWithStyle(ColourUtils.RED), true);
                    return 0;
                }
                if (!discordSocket.connected()) {
                    McUtils.sendLocalMessage(Text.literal("§eConnecting to chat server..."),
                            Prepend.GUILD.getWithStyle(ColourUtils.YELLOW), true);
                    connectAttempt = 1;
                    discordSocket.connect();
                    return Command.SINGLE_SUCCESS;
                } else {
                    McUtils.sendLocalMessage(Text.literal("§aYou are already connected to the chat server!"),
                            Prepend.GUILD.getWithStyle(ColourUtils.GREEN), true);
                    return 0;
                }
            }));
        });
    }
}
