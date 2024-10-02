package pixlze.guildapi.net.type;

import pixlze.guildapi.GuildApi;
import pixlze.guildapi.net.event.NetEvents;

import java.util.List;

public class Api {
    public final String name;
    private final List<Class<? extends Api>> dependencies;
    public boolean crashed = false;
    protected String baseURL;
    protected boolean enabled = false;
    private int missingDeps;

    protected Api(String name, List<Class<? extends Api>> dependencies) {
        this.name = name;
        this.dependencies = dependencies;
        missingDeps = dependencies.size();
        NetEvents.LOADED.register(this::onApiLoaded);
    }

    private void onApiLoaded(Api api) {
        if (this.depends(api)) dependencyLoaded();
    }

    public boolean depends(Api api) {
        return dependencies.contains(api.getClass());
    }

    private void dependencyLoaded() {
        --missingDeps;
        if (missingDeps == 0) {
            ready();
        }
    }

    protected void ready() {
        init();
    }

    public void init() {
        enabled = true;
        NetEvents.LOADED.invoker().interact(this);
    }

    public void crash() {
        GuildApi.LOGGER.warn("{} services crashing", name);
        enabled = false;
        crashed = true;
        missingDeps = dependencies.size();
    }
}
