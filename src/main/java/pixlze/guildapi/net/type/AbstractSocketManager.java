package pixlze.guildapi.net.type;

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
import pixlze.guildapi.core.components.Manager;
import pixlze.guildapi.utils.ColourUtils;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.type.Prepend;

import java.util.*;
import java.util.function.Consumer;

public abstract class AbstractSocketManager extends Manager {
    private final ArrayList<Pair<String, Consumer<Object[]>>> listeners = new ArrayList<>();
    private boolean enabled = false;
    public Socket socket;
    protected boolean firstConnect = true;
    protected int connectAttempt = 0;
    protected final IO.Options options = IO.Options.builder()
            .setExtraHeaders(new HashMap<>(Map.of("user" +
                    "-agent", Collections.singletonList(GuildApi.MOD_ID + "/" + GuildApi.MOD_VERSION))))
            .setTimeout(60000)
            .setReconnection(false)
            .build();

    public AbstractSocketManager(List<Manager> dependencies) {
        super(dependencies);
    }

    public void emit(String event, Object data) {
        if (socket != null && socket.connected()) {
            GuildApi.LOGGER.info("emitting, {}", data);
            socket.emit(event, data);
        } else {
            GuildApi.LOGGER.warn("skipped event because of missing or inactive socket");
        }
    }

    protected void resetConnection() {
        if (socket != null)
            socket.disconnect();
        firstConnect = true;
        connectAttempt = 0;
    }

    public boolean isDisabled() {
        return !enabled;
    }

    public void enable() {
        if (!enabled) {
            if (tryConnect()) enabled = true;
        }
    }

    public void disable() {
        if (enabled) {
            enabled = false;
            resetConnection();
        }
    }

    /** Tries to connect based on connection conditions. */
    protected abstract boolean tryConnect();

    protected abstract String getToken();

    private void registerDefaultListeners() {
        registerListener(Socket.EVENT_DISCONNECT, (reason) -> {
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
                socket.connect();
            } catch (InterruptedException e) {
                GuildApi.LOGGER.error("thread sleep error: {} {}", e, e.getMessage());
            }
        });

        registerListener(Socket.EVENT_CONNECT_ERROR, (err) -> {
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
                        options.extraHeaders.put("Authorization", Collections.singletonList("bearer " + getToken()));
                } catch (Exception e) {
                    GuildApi.LOGGER.error("connect error: {} {}", e, e.getMessage());
                }
            }
            try {
                Thread.sleep(1000);
                if (++connectAttempt < 10) {
                    socket.disconnect();
                    socket.connect();
                } else
                    McUtils.sendLocalMessage(Text.literal("§cCould not connect to chat server. Type /reconnect to try again."),
                            Prepend.GUILD.getWithStyle(ColourUtils.RED), true);
            } catch (Exception e) {
                GuildApi.LOGGER.error("reconnect discord error: {} {}", e, e.getMessage());
            }
        });

        registerListener(Socket.EVENT_CONNECT, (args) -> {
            McUtils.sendLocalMessage(Text.literal("§aSuccessfully connected to chat server."),
                    Prepend.GUILD.getWithStyle(Style.EMPTY.withColor(Formatting.GREEN)), true);
            firstConnect = false;
        });
    }

    /** Update the socket object to reflect changes to socket options. */
    protected abstract void updateSocket();

    protected void initSocket(boolean reloadSocket) {
        if (reloadSocket) {
            firstConnect = true;
            updateSocket();
            for (Pair<String, Consumer<Object[]>> listener : listeners) {
                registerListener(listener.getLeft(), listener.getRight());
            }
            registerDefaultListeners();
        }
        enable();
    }

    public void saveListener(String name, Consumer<Object[]> listener) {
        listeners.add(new Pair<>(name, listener));
        registerListener(name, listener);
    }

    public void registerListener(String name, Consumer<Object[]> listener) {
        if (socket != null)
            socket.on(name, listener::accept);
        else GuildApi.LOGGER.warn("tried to register listener for event: {} while socket was null", name);
    }

    protected abstract boolean doConnect();

    @Override
    public void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("reconnect").executes((context) -> {
                if (isDisabled()) {
                    McUtils.sendLocalMessage(Text.literal("§cCannot connect to chat server at this time. Please join a world first."), Prepend.GUILD.getWithStyle(ColourUtils.RED), true);
                    return 0;
                }
                if (doConnect()) {
                    if (!socket.connected()) {
                        McUtils.sendLocalMessage(Text.literal("§eConnecting to chat server..."),
                                Prepend.GUILD.getWithStyle(ColourUtils.YELLOW), true);
                        connectAttempt = 1;
                        socket.connect();
                        return Command.SINGLE_SUCCESS;
                    } else {
                        McUtils.sendLocalMessage(Text.literal("§aYou are already connected to the chat server!"),
                                Prepend.GUILD.getWithStyle(ColourUtils.GREEN), true);
                        return 0;
                    }
                }
                return 0;
            }));
        });
    }
}
