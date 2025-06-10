package pixlze.guildapi.net;

import pixlze.guildapi.core.handlers.connection.event.WynncraftConnectionEvents;
import pixlze.guildapi.net.type.Api;

import java.util.ArrayList;
import java.util.List;

public class WynnJoinApi extends Api {
    protected WynnJoinApi() {
        super("join", List.of());
    }

    private final List<Runnable> joinTasks = new ArrayList<>();

    @Override
    public void init() {
        WynncraftConnectionEvents.JOIN.register(this::onWynnJoin);
        WynncraftConnectionEvents.LEAVE.register(this::onWynnLeave);
    }

    public void addTask(Runnable task) {
        joinTasks.add(task);
    }

    private void onWynnJoin() {
        for (Runnable task : joinTasks) {
            task.run();
        }
        joinTasks.clear();
        this.enable();
    }

    private void onWynnLeave() {
        this.disable();
    }
}
