package pixlze.guildapi.net;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
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
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.components.Models;
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
    private final HashSet<Pair<String, Consumer<Object[]>>> listeners = new HashSet<>();
    public Socket discordSocket;
    private boolean firstConnect = true;
    private int connectAttempt = 0;
    private GuildApiClient guild;
    private String guildPrefix;
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
    protected void ready() {
        guild = Managers.Net.guild;

        options.extraHeaders.put("from", Collections.singletonList(McUtils.playerName()));
        boolean reloadSocket = false;
        if (!guild.guildPrefix.equals(guildPrefix)) {
            guildPrefix = guild.guildPrefix;
            reloadSocket = true;
        }
        initSocket(reloadSocket);
    }

    private void initSocket(boolean reloadSocket) {
        GuildApi.LOGGER.info("initializing sockets");
        if (reloadSocket) {
            firstConnect = true;
            options.extraHeaders.put("Authorization", Collections.singletonList("bearer " + guild.getToken(true)));
            discordSocket = IO.socket(URI.create(guild.getBaseURL() + "discord"), options);
            for (Pair<String, Consumer<Object[]>> listener : listeners) {
                addDiscordListener(listener.getLeft(), listener.getRight());
            }

            addDiscordListener(Socket.EVENT_DISCONNECT, (reason) -> {
                connectAttempt = 0;

                McUtils.sendLocalMessage(Text.literal("§cDisconnected from chat server."),
                        Prepend.GUILD.getWithStyle(Style.EMPTY.withColor(Formatting.RED)), true);
                GuildApi.LOGGER.info("{} disconnected", reason);

                try {
                    Thread.sleep(1000);
                    discordSocket.connect();
                } catch (InterruptedException e) {
                    GuildApi.LOGGER.error("thread sleep error: {} {}", e, e.getMessage());
                }
            });

            addDiscordListener(Socket.EVENT_CONNECT_ERROR, (err) -> {
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
                    if (++connectAttempt < 10)
                        discordSocket.connect();
                    else
                        McUtils.sendLocalMessage(Text.literal("§cCould not connect to chat server. Type /reconnect to try again."),
                                Prepend.GUILD.getWithStyle(ColourUtils.RED), true);
                } catch (Exception e) {
                    GuildApi.LOGGER.error("reconnect discord error: {} {}", e, e.getMessage());
                }
            });

            addDiscordListener(Socket.EVENT_CONNECT, (args) -> {
                McUtils.sendLocalMessage(Text.literal("§aSuccessfully connected to chat server."),
                        Prepend.GUILD.getWithStyle(Style.EMPTY.withColor(Formatting.GREEN)), true);
                firstConnect = false;
            });
        }
        if (GuildApi.isDevelopment() || Models.WorldState.onWorld()) {
            discordSocket.connect();
            GuildApi.LOGGER.info("sockets connecting");
        }
        WorldStateEvents.CHANGE.register(this::worldStateChanged);
        super.enable();
    }

    public void addDiscordListener(String name, Consumer<Object[]> listener) {
        listeners.add(new Pair<>(name, listener));
        if (discordSocket != null)
            discordSocket.on(name, listener::accept);
    }

    private void worldStateChanged(WorldState state) {
        if (isDisabled()) return;
        if (state == WorldState.WORLD) {
            if (!discordSocket.connected()) {
                discordSocket.connect();
                GuildApi.LOGGER.info("discord socket on");
            }
        } else {
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
            if (GuildApi.isDevelopment()) {
                dispatcher.register(ClientCommandManager.literal("testmessage")
                        .then(ClientCommandManager.argument("message", StringArgumentType.greedyString())
                                .executes((context) -> {
                                    emit(discordSocket, "wynnMessage", StringArgumentType.getString(context, "message")
                                            .replaceAll("&", "§"));
                                    return Command.SINGLE_SUCCESS;
                                })));
            }
        });
    }

    @Override
    protected void unready() {
        super.unready();
        discordSocket.disconnect();
    }
}
