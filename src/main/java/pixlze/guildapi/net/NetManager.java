package pixlze.guildapi.net;

import net.minecraft.text.Text;
import pixlze.guildapi.net.type.Api;
import pixlze.guildapi.utils.McUtils;

import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.Map;

public class NetManager {
    // dependency : connection manager
    public static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    // TODO add event for when an api is loaded so that things that depend on it can realize its loaded (right now im
    //  hardcoding dependencies, in the future have one single "all dependencies loaded function" for getting values
    //  from all dependencies)
    private final Map<String, Api> apis = new HashMap<>();

    public void apiCrash(Text message, Api api) {
        McUtils.sendLocalMessage(message);
        for (Api a : apis.values()) {
            if (a.equals(api) || a.depends(api)) {
                a.crash();
            }
        }
    }

    public <T extends Api> T getApi(String name, Class<T> apiClass) {
        Api api = apis.get(name);
        if (apiClass.isInstance(api)) return apiClass.cast(api);
        throw new IllegalArgumentException("API not found or wrong type: " + name);
    }

    public void init() {
        registerApi(new WynnApiManager());
        registerApi(new GuildApiManager());
        registerApi(new SocketIOManager());
        initApis();
    }

    private void registerApi(Api api) {
        apis.put(api.name, api);
    }

    private void initApis() {
        for (Api a : apis.values()) {
            if (!a.crashed) a.init();
        }
    }
}
