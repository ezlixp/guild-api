package pixlze.guildapi.net.type;

import pixlze.guildapi.GuildApi;

import java.util.List;

public class Api {
    public final String name;
    private final List<Api> dependencies;
    public boolean crashed = false;
    protected String baseURL;
    protected boolean enabled = false;

    protected Api(String name, List<Api> dependencies) {
        this.name = name;
        this.dependencies = dependencies;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void crash() {
        GuildApi.LOGGER.warn("{} services crashing", name);
        enabled = false;
    }

    public boolean depends(Api api) {
        return dependencies.contains(api);
    }

    public void init() {
        enabled = true;
    }
}
