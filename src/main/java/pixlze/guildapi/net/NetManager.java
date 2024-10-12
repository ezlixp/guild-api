package pixlze.guildapi.net;

import net.minecraft.text.Text;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.net.type.Api;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.type.Prepend;

import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.Map;

public class NetManager {
    public static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private final Map<String, Api> apis = new HashMap<>();
    public WynnApiClient wynn = new WynnApiClient();
    public GuildApiClient guild = new GuildApiClient();
    public SocketIOClient socket = new SocketIOClient();

    public void apiCrash(Text message, Api api) {
        McUtils.sendLocalMessage(message, Prepend.DEFAULT.get(), false);
        api.disable();
    }
    
    @Deprecated
    public <T extends Api> T getApi(String name, Class<T> apiClass) {
        Api api = apis.get(name);
        if (apiClass.isInstance(api)) return apiClass.cast(api);
        GuildApi.LOGGER.error("Requested api \"{}\" does not exist/has not been loaded.", name);
        return null;
    }

    public void init() {
        registerApi(wynn);
        registerApi(guild);
        registerApi(socket);
        initApis();
    }

    private <T extends Api> void registerApi(T api) {
        apis.put(api.name, api);
    }

    private void initApis() {
        for (Api a : apis.values()) {
            if (!a.enabled) a.init();
        }
    }
}
