package pixlze.guildapi.net;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.socket.client.IO;
import io.socket.client.Socket;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.components.Models;
import pixlze.guildapi.models.event.WorldStateEvents;
import pixlze.guildapi.models.type.WorldState;
import pixlze.guildapi.net.type.Api;
import pixlze.guildapi.utils.McUtils;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


public class SocketIOClient extends Api {
    private static SocketIOClient instance;
    private Socket aspectSocket;
    private Socket discordSocket;
    private GuildApiClient guild;

    public SocketIOClient() {
        super("socket", List.of(GuildApiClient.class));
        if (GuildApi.isDevelopment()) {
            ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
                dispatcher.register(ClientCommandManager.literal("emit").executes((context) -> {
                    aspectEmit("give_aspect", Collections.singletonMap("player", "test"));
                    return 0;
                }));
                dispatcher.register(ClientCommandManager.literal("testmessage")
                        .then(ClientCommandManager.argument("message", StringArgumentType.word())
                                .executes((context) -> {
                                    discordEmit("send", Map.of("username", McUtils.playerName(), "message", StringArgumentType.getString(context, "message")));
                                    return 0;
                                })));
            });
        }
        instance = this;
    }

    public void aspectEmit(String event, Map<?, ?> data) {
        if (aspectSocket != null && aspectSocket.connected()) aspectSocket.emit(event, data);
        else GuildApi.LOGGER.warn("skipped event because of missing or inactive aspect socket");
    }

    public void discordEmit(String event, Object data) {
        if (discordSocket != null && discordSocket.connected()) {
            GuildApi.LOGGER.info("emitting, {}", data);
            discordSocket.emit(event, data);
        } else GuildApi.LOGGER.warn("skipped event because of missing or inactive discord socket");
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
                .setExtraHeaders(Collections.singletonMap("authorization", Collections.singletonList("bearer " + guild.getToken())))
                .build();
        aspectSocket = IO.socket(URI.create(guild.getBaseURL() + "aspects"), options);
        discordSocket = IO.socket(URI.create(guild.getBaseURL() + "discord"), options);
        if (GuildApi.isDevelopment() || Models.WorldState.onWorld()) {
            aspectSocket.connect();
            discordSocket.connect();
            GuildApi.LOGGER.info("sockets on");
        }
        WorldStateEvents.CHANGE.register(this::worldStateChanged);
        super.enable();
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

    public void addDiscordListener(String name, Consumer<Object[]> listener) {
        discordSocket.on(name, listener::accept);
    }
}
