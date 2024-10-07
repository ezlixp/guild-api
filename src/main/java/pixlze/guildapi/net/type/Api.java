package pixlze.guildapi.net.type;

import pixlze.guildapi.GuildApi;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.net.event.NetEvents;

import java.util.List;

public abstract class Api {
    public final String name;
    private final List<Class<? extends Api>> dependencies;
    public boolean enabled = false;
    protected String baseURL;
    private int missingDeps;

    // TODO move api get posts here
    protected Api(String name, List<Class<? extends Api>> dependencies) {
        this.name = name;
        this.dependencies = dependencies;
        missingDeps = dependencies.size();
        NetEvents.LOADED.register(this::onApiLoaded);
        NetEvents.DISABLED.register(this::onApiDisabled);
    }

    private void onApiLoaded(Api api) {
        if (this.depends(api)) dependencyLoaded();
    }

    private void onApiDisabled(Api api) {
        if (this.depends(api)) ++missingDeps;
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
        if (!enabled) {
            enabled = true;
            NetEvents.LOADED.invoker().interact(this);
        }
    }

    public abstract <T extends Api> T getInstance();

    public void disable() {
        GuildApi.LOGGER.warn("{} disabling service", name);
        enabled = false;
        for (Api api : Managers.Net.getDependsOn(this)) {
            if (api.enabled) api.disable();
        }
    }
}
