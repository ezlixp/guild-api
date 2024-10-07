package pixlze.guildapi.net;

import net.minecraft.text.Text;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.net.type.Api;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.type.Prepend;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetManager {
    public static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private final Map<String, Api> apis = new HashMap<>();

    public void apiCrash(Text message, Api api) {
        McUtils.sendLocalMessage(message, Prepend.DEFAULT.get());
        api.disable();
    }

    public List<Api> getDependsOn(Api parent) {
        ArrayList<Api> out = new ArrayList<>();
        for (Api api : apis.values()) {
            if (api.depends(parent)) out.add(api);
        }
        return out;
    }

    public <T extends Api> T getApi(String name, Class<T> apiClass) {
        Api api = apis.get(name);
        if (apiClass.isInstance(api)) return apiClass.cast(api);
        GuildApi.LOGGER.error("Requested api \"{}\" does not exist/has not been loaded.", name);
        return null;
    }

    public void init() {
        registerApi(new WynnApiClient());
        registerApi(new GuildApiClient());
        registerApi(new SocketIOClient());
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
