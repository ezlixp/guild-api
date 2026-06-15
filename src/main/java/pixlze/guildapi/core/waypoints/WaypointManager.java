package pixlze.guildapi.core.waypoints;

import pixlze.guildapi.core.components.Manager;
import pixlze.guildapi.core.handlers.connection.event.WynncraftConnectionEvents;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WaypointManager extends Manager {
    private final Map<String, Waypoint> waypoints = new HashMap<>();


    public WaypointManager() {
        super(List.of());
    }

    @Override
    public void init() {
        WynncraftConnectionEvents.JOIN.register(this::onWynnJoin);
    }

    private void onWynnJoin() {
        waypoints.put("hi", new Waypoint("39365bd4-5c78-41de-8901-c7dc5b7c64c4", "pixlze", -1177, 100, -2455));
    }

}
