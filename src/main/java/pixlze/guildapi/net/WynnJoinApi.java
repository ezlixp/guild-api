package pixlze.guildapi.net;

import pixlze.guildapi.core.handlers.connection.event.WynncraftConnectionEvents;
import pixlze.guildapi.net.type.Api;

import java.util.List;

public class WynnJoinApi extends Api {
    protected WynnJoinApi() {
        super("join", List.of());
    }

    @Override
    public void init() {
        WynncraftConnectionEvents.JOIN.register(this::onWynnJoin);
        WynncraftConnectionEvents.LEAVE.register(this::onWynnLeave);
    }

    private void onWynnJoin() {
        this.enable();
    }

    private void onWynnLeave() {
        this.disable();
    }
}
