package pixlze.guildapi.net;

import net.minecraft.text.Text;
import pixlze.guildapi.net.type.Api;
import pixlze.guildapi.utils.McUtils;

import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.Map;

public class NetManager {
    public static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
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
        registerApi(new WynnApiClient());
        registerApi(new GuildApiClient());
        registerApi(new SocketIOClient());
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
