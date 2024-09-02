package pixlze.guildapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pixlze.guildapi.components.Handlers;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.json.type_adapters.PairAdapter;
import pixlze.guildapi.json.type_adapters.PatternAdapter;
import pixlze.guildapi.net.ApiManager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.regex.Pattern;


public class GuildApi implements ClientModInitializer {
    public static final String MOD_ID = "guildapi";
    public static final Logger LOGGER = LoggerFactory.getLogger("guildapi");
    public static Gson gson;
    public static KeyBinding openConfigKeybind;
    public static JsonObject secrets;

    @Override
    public void onInitializeClient() {
        System.setProperty("java.awt.headless", "false");

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Pattern.class, new PatternAdapter().nullSafe());
        builder.registerTypeAdapter(Pair.class, new PairAdapter().nullSafe());
        gson = builder.create();

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("secrets.json");
        if (inputStream == null) {
            GuildApi.LOGGER.error("secret variables could not be loaded");
        } else {
            secrets = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
        }


        ApiManager.init();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (ApiManager.guildRaidTokenCreatedOn != null && new Date().getTime() - ApiManager.guildRaidTokenCreatedOn.getTime() >= 72000000) {
                GuildApi.LOGGER.info("refreshing token");
                ApiManager.refreshGuildRaidServerToken();
            }
        });


        Handlers.Chat.init();
        Managers.Connection.init();
    }
}