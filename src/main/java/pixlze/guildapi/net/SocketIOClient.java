package pixlze.guildapi.net;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.socket.client.IO;
import io.socket.client.Socket;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.components.Models;
import pixlze.guildapi.models.event.WorldStateEvents;
import pixlze.guildapi.models.type.WorldState;
import pixlze.guildapi.net.type.Api;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.type.Prepend;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


public class SocketIOClient extends Api {
    private static SocketIOClient instance;
    public Socket discordSocket;
    private GuildApiClient guild;
    private String guildPrefix;
    private final HashSet<Pair<String, Consumer<Object[]>>> listeners = new HashSet<>();

    public SocketIOClient() {
        super("socket", List.of(GuildApiClient.class));
        if (GuildApi.isDevelopment()) {
            ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
                dispatcher.register(ClientCommandManager.literal("testmessage")
                        .then(ClientCommandManager.argument("message", StringArgumentType.greedyString())
                                .executes((context) -> {
                                    emit(discordSocket, "wynnMessage", StringArgumentType.getString(context, "message").replaceAll("&", "§"));
                                    return 0;
                                })));
            });
        }
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

    @Override
    public SocketIOClient getInstance() {
        return instance;
    }

    @Override
    protected void ready() {
        guild = Managers.Net.guild;
        boolean reloadSocket = false;
        if (!guild.guildPrefix.equals(guildPrefix)) {
            guildPrefix = guild.guildPrefix;
            reloadSocket = true;
        }
        initSocket(reloadSocket);
    }

    private void initSocket(boolean reloadSocket) {
        IO.Options options = IO.Options.builder()
                .setExtraHeaders(Map.of("authorization", Collections.singletonList("bearer " + guild.getToken()), "from", Collections.singletonList(McUtils.playerName()), "user" +
                        "-agent", Collections.singletonList(GuildApi.MOD_ID + "/" + GuildApi.MOD_VERSION)))
                .setTimeout(60000)
                .build();
        if (reloadSocket) {
            discordSocket = IO.socket(URI.create(guild.getBaseURL() + "discord"), options);
            for (Pair<String, Consumer<Object[]>> listener : listeners) {
                addDiscordListener(listener.getLeft(), listener.getRight());
            }
            addDiscordListener("connect_error", (err) -> McUtils.sendLocalMessage(Text.literal("§cCould not connect to chat server."),
                    Prepend.GUILD.getWithStyle(Style.EMPTY.withColor(Formatting.RED)), true));
            addDiscordListener("connect", (args) -> McUtils.sendLocalMessage(Text.literal("§aSuccessfully connected to chat server."),
                    Prepend.GUILD.getWithStyle(Style.EMPTY.withColor(Formatting.GREEN)), true));
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

    }

    @Override
    protected void unready() {
        super.unready();
        discordSocket.disconnect();
    }
}
