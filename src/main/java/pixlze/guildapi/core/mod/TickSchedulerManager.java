package pixlze.guildapi.core.mod;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import pixlze.guildapi.core.Manager;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TickSchedulerManager extends Manager {
    private final Map<Runnable, Integer> tasks = new ConcurrentHashMap<>();


    @Override
    public void init() {
        ClientTickEvents.START_CLIENT_TICK.register(this::onTick);
    }


    public void scheduleLater(Runnable runnable, int ticksDelay) {
        tasks.put(runnable, ticksDelay);
    }

    public void scheduleNextTick(Runnable runnable) {
        tasks.put(runnable, 0);
    }

    public void onTick(MinecraftClient client) {
        Iterator<Map.Entry<Runnable, Integer>> it = tasks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Runnable, Integer> entry = it.next();
            int ticksLeft = entry.getValue();
            if (ticksLeft == 0) {
                entry.getKey().run();
                it.remove();
            } else {
                entry.setValue(ticksLeft - 1);
            }
        }
    }
}
