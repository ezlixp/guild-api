package pixlze.guildapi.net;

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

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class SocketIOManager extends Api {
    private Socket socket;
    private GuildApiManager guild;

    public SocketIOManager() {
        super("socket", List.of(Managers.Net.getApi("guild", GuildApiManager.class)));
        if (GuildApi.isDevelopment()) {
            ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
                dispatcher.register(ClientCommandManager.literal("socket").executes((context) -> {
                            socket.close().open();
                            return 0;
                        })
                );
                dispatcher.register(ClientCommandManager.literal("emit").executes((context) -> {
                            emitEvent("give_aspect", Collections.singletonMap("player", "test"));
                            return 0;
                        })
                );
                dispatcher.register(ClientCommandManager.literal("index").executes((context) -> {
                            emitEvent("debug_index", null);
                            return 0;
                        })
                );
            });
        }
    }

    public void emitEvent(String event, Map<?, ?> data) {
        if (socket != null)
            socket.emit(event, data);
    }

    @Override
    protected void ready() {
        crashed = false;
        guild = Managers.Net.getApi("guild", GuildApiManager.class);
        initSocket();
    }

    private void initSocket() {
        IO.Options options = IO.Options.builder()
                .setExtraHeaders(Collections.singletonMap("authorization", Collections.singletonList("bearer " + guild.getToken())))
                .build();
        socket = IO.socket(URI.create(guild.getBaseURL() + "aspects"), options);
        if (GuildApi.isDevelopment() || Models.WorldState.onWorld()) {
            GuildApi.LOGGER.info("socket on main");
            socket.connect();
            socket.emit("sync");
        }
        WorldStateEvents.CHANGE.register(this::worldStateChanged);
        super.init();
    }

    private void worldStateChanged(WorldState state) {
        if (state == WorldState.WORLD) {
            if (!socket.isActive()) {
                socket.connect();
                socket.emit("sync");
                GuildApi.LOGGER.info("socket on");
            }
        } else {
            if (socket.isActive()) {
                socket.disconnect();
                GuildApi.LOGGER.info("socket off");
            }
        }
    }
}
