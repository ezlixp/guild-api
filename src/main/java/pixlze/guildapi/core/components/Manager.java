package pixlze.guildapi.core.components;

import java.util.List;

public abstract class Manager {
    public Manager(List<Manager> dependencies) {

    }

    public abstract void init();
}
