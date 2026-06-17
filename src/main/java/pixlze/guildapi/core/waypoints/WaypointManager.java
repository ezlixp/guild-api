package pixlze.guildapi.core.waypoints;

import pixlze.guildapi.core.components.Manager;
import pixlze.guildapi.models.Models;
import pixlze.guildapi.models.worldState.event.WorldStateEvents;
import pixlze.guildapi.models.worldState.type.WorldState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class WaypointManager extends Manager {
    private final Map<String, Waypoint> waypoints = new HashMap<>();


    public WaypointManager() {
        super(List.of());
    }

    @Override
    public void init() {
        WorldStateEvents.CHANGE.register(this::onWorldState);
    }

    private void onWorldState(WorldState newWorldState) {
        setAllEnabled(newWorldState == WorldState.WORLD);
    }

    public void setAllEnabled(boolean enabled) {
        for (Map.Entry<String, Waypoint> waypoint : waypoints.entrySet()) {
            waypoint.getValue().setEnabled(enabled);
            if (enabled)
                waypoint.getValue().tryShow();
            else waypoint.getValue().hide();
        }
    }

    public void setAllActive(Function<String, Boolean> condition) {
        for (Map.Entry<String, Waypoint> waypoint : waypoints.entrySet()) {
            if (condition.apply(waypoint.getKey())) {
                waypoint.getValue().setActive(true);
                waypoint.getValue().tryShow();
            } else {
                waypoint.getValue().setActive(false);
                waypoint.getValue().hide();
            }
        }

    }

    public void addWaypoint(String name, double x, double y, double z, boolean active) {
        if (waypoints.containsKey(name)) {
            waypoints.get(name).update(x, y, z);
        } else {
            waypoints.put(name, new Waypoint(name, x, y, z));
            waypoints.get(name).setEnabled(Models.WorldState.onWorld());
            waypoints.get(name).setActive(active);
            waypoints.get(name).tryShow();
        }
    }

    public void removeWaypoint(String name) {
        if (waypoints.containsKey(name)) {
            waypoints.get(name).hide();
            waypoints.remove(name);
        }
    }

    public void clearWaypoints() {
        setAllEnabled(false);
        waypoints.clear();
    }

    public Map<String, Waypoint> getWaypoints() {
        return waypoints;
    }

}
