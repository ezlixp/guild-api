package pixlze.guildapi.net;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.socket.client.IO;
import io.socket.client.Socket;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


public class SocketIOClient extends Api {
    private static SocketIOClient instance;
    public Socket aspectSocket;
    public Socket discordSocket;
    private GuildApiClient guild;

    public SocketIOClient() {
        super("socket", List.of(GuildApiClient.class));
        if (GuildApi.isDevelopment()) {
            ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
                dispatcher.register(ClientCommandManager.literal("emit").executes((context) -> {
                    emit(aspectSocket, "give_aspect", Collections.singletonMap("player", "test"));
                    return 0;
                }));
                dispatcher.register(ClientCommandManager.literal("testmessage")
                        .then(ClientCommandManager.argument("message", StringArgumentType.word())
                                .executes((context) -> {
                                    emit(discordSocket, "send", Map.of("username", McUtils.playerName(), "message", StringArgumentType.getString(context, "message")));
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
            McUtils.sendLocalMessage(Text.literal("§eChat server not connected"), Prepend.GUILD.get());
            GuildApi.LOGGER.warn("skipped event because of missing or inactive socket");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public SocketIOClient getInstance() {
        return instance;
    }

    @Override
    protected void ready() {
        guild = Managers.Net.getApi("guild", GuildApiClient.class);
        initSocket();
    }

    private void initSocket() {
        IO.Options options = IO.Options.builder()
                .setExtraHeaders(Map.of("authorization", Collections.singletonList("bearer " + guild.getToken()), "from", Collections.singletonList(McUtils.playerName())))
                .build();
        aspectSocket = IO.socket(URI.create(guild.getBaseURL() + "aspects"), options);
        discordSocket = IO.socket(URI.create(guild.getBaseURL() + "discord"), options);
        addDiscordListener("connect_error", (err) -> McUtils.sendLocalMessage(Text.literal("§cCould not connect to chat server."), Prepend.GUILD.getWithStyle(Style.EMPTY.withColor(Formatting.RED))));
        addDiscordListener("connect", (args) -> McUtils.sendLocalMessage(Text.literal("§aSuccessfully connected to chat server."), Prepend.GUILD.getWithStyle(Style.EMPTY.withColor(Formatting.GREEN))));
        if (GuildApi.isDevelopment() || Models.WorldState.onWorld()) {
            aspectSocket.connect();
            discordSocket.connect();
            GuildApi.LOGGER.info("sockets connecting");
        }
        WorldStateEvents.CHANGE.register(this::worldStateChanged);
        super.enable();
    }

    public void addDiscordListener(String name, Consumer<Object[]> listener) {
        discordSocket.on(name, listener::accept);
    }

    private void worldStateChanged(WorldState state) {
        if (!enabled) return;
        if (state == WorldState.WORLD) {
            if (!aspectSocket.connected()) {
                aspectSocket.connect();
                GuildApi.LOGGER.info("aspect socket on");
            }
            if (!discordSocket.connected()) {
                discordSocket.connect();
                GuildApi.LOGGER.info("discord socket on");
            }
        } else {
            if (aspectSocket.connected()) {
                aspectSocket.disconnect();
                GuildApi.LOGGER.info("aspect socket off");
            }
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
        aspectSocket.disconnect();
        discordSocket.disconnect();
    }
}
