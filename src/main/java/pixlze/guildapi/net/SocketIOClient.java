package pixlze.guildapi.net;

import io.socket.client.IO;
import io.socket.client.Socket;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.components.Models;
import pixlze.guildapi.handlers.chat.event.ChatMessageReceived;
import pixlze.guildapi.models.event.WorldStateEvents;
import pixlze.guildapi.models.type.WorldState;
import pixlze.guildapi.net.type.Api;
import pixlze.guildapi.utils.TextUtils;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SocketIOClient extends Api {
    private final Pattern guildForegroundPattern = Pattern.compile("^§b((\uDAFF\uDFFC\uE006\uDAFF\uDFFF\uE002\uDAFF\uDFFE)|(\uDAFF\uDFFC\uE001\uDB00\uDC06)).*§3(.*):§b (.*)$");
    private final Pattern guildBackgroundPattern = Pattern.compile("^§8((\uDAFF\uDFFC\uE006\uDAFF\uDFFF\uE002\uDAFF\uDFFE)|(\uDAFF\uDFFC\uE001\uDB00\uDC06)).*§8(.*):§8 (.*)$");
    private Socket aspectSocket;
    private Socket discordSocket;
    private GuildApiClient guild;

    public SocketIOClient() {
        super("socket", List.of(GuildApiClient.class));
        if (GuildApi.isDevelopment()) {
            ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
                dispatcher.register(ClientCommandManager.literal("socket").executes((context) -> {
                    aspectSocket.disconnect().connect();
                    return 0;
                }));
                dispatcher.register(ClientCommandManager.literal("emit").executes((context) -> {
                    aspectEmit("give_aspect", Collections.singletonMap("player", "test"));
                    return 0;
                }));
                dispatcher.register(ClientCommandManager.literal("index").executes((context) -> {
                    aspectEmit("debug_index", null);
                    return 0;
                }));
            });
        }
    }

    public void aspectEmit(String event, Map<?, ?> data) {
        if (aspectSocket != null && aspectSocket.connected()) aspectSocket.emit(event, data);
        else GuildApi.LOGGER.warn("skipped event because of missing or inactive aspect socket");
    }

    @Override
    protected void ready() {
        crashed = false;
        guild = Managers.Net.getApi("guild", GuildApiClient.class);
        initSocket();
        ChatMessageReceived.EVENT.register((message) -> {
            String m = TextUtils.parseStyled(message, "§", "");
            GuildApi.LOGGER.info("received: {}", message.getString());
            Matcher foregroundMatcher = guildForegroundPattern.matcher(m);
            Matcher backgroundMatcher = guildBackgroundPattern.matcher(m);
            if (foregroundMatcher.find()) {
                String username = foregroundMatcher.group(4);
                List<String> usernames = TextUtils.extractUsernames(message);
                if (!usernames.isEmpty()) {
                    username = usernames.getFirst();
                }
                discordEmit("send", Map.of("username", username, "message", foregroundMatcher.group(5)));
            } else if (backgroundMatcher.find()) {
                String username = backgroundMatcher.group(4);
                List<String> usernames = TextUtils.extractUsernames(message);
                if (!usernames.isEmpty()) {
                    username = usernames.getFirst();
                }
                discordEmit("send", Map.of("username", username, "message", backgroundMatcher.group(5)));
            }
        });
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
        }
        WorldStateEvents.CHANGE.register(this::worldStateChanged);
        super.init();
    }

    public void discordEmit(String event, Map<String, Object> data) {
        if (discordSocket != null && discordSocket.connected()) {
            GuildApi.LOGGER.info("emitting, {}", data.getOrDefault("message", "doesn't exist"));
            discordSocket.emit(event, data);
        } else GuildApi.LOGGER.warn("skipped event because of missing or inactive discord socket");
    }

    private void worldStateChanged(WorldState state) {
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
}
