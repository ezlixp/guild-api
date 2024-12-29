package pixlze.guildapi.models.worldState;

import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.mc.event.PlayerInfoChangedEvents;
import pixlze.guildapi.mod.event.WynncraftConnectionEvents;
import pixlze.guildapi.models.worldState.event.WorldStateEvents;
import pixlze.guildapi.models.worldState.type.WorldState;

import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

public class WorldStateModel {
    private static final Pattern HUB_NAME = Pattern.compile("^\n§6§l play.wynncraft.com \n$");
    private static final UUID WORLD_NAME_UUID = UUID.fromString("16ff7452-714f-2752-b3cd-c3cb2068f6af");
    private static final Pattern WORLD_NAME = Pattern.compile("^§f {2}§lGlobal \\[(.*)]$");
    private static final Vec3d AFK_QUEUE_POSITION = new Vec3d(8.5, 65, 8.5);
    private WorldState currentState = WorldState.NOT_CONNECTED;
    private Text currentTabListFooter = Text.empty();

    public void init() {
        WynncraftConnectionEvents.JOIN.register(this::connecting);
        WynncraftConnectionEvents.LEAVE.register(this::disconnected);
        WynncraftConnectionEvents.CHANGE.register(this::changing);
        PlayerInfoChangedEvents.DISPLAY.register(this::onDisplayChanged);
        PlayerInfoChangedEvents.FOOTER.register(this::onTabListFooter);
    }

    public void connecting() {
        if (GuildApi.isDevelopment()) {
            setState(WorldState.WORLD);
            return;
        }
        setState(WorldState.CONNECTING);
        currentTabListFooter = Text.empty();
    }

    public void disconnected() {
        setState(WorldState.NOT_CONNECTED);
    }

    public void changing() {
        if (currentState == WorldState.WORLD)
            setState(WorldState.CONNECTING);
    }

    public void onDisplayChanged(UUID uuid, Text text) {
        if (!uuid.equals(WORLD_NAME_UUID)) return;
        if (WORLD_NAME.matcher(text.getString()).find()) {
            setState(WorldState.WORLD);
        }
    }

    public void onTabListFooter(Text footer) {
        if (footer.equals(currentTabListFooter)) return;
        currentTabListFooter = footer;
        if (footer.getLiteralString() != null) {
            if (HUB_NAME.matcher(Objects.requireNonNull(footer.getLiteralString())).find()) {
                setState(WorldState.HUB);
            }
        }
    }

    private void setState(WorldState state) {
        if (currentState != state) {
            currentState = state;
            WorldStateEvents.CHANGE.invoker().changed(state);
        }
    }

    public boolean onWorld() {
        return currentState == WorldState.WORLD;
    }
}
