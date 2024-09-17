package pixlze.guildapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.json.type_adapters.PairAdapter;
import pixlze.guildapi.json.type_adapters.PatternAdapter;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;


public class GuildApi implements ClientModInitializer {
    public static final String MOD_ID = "guildapi";
    public static final String MOD_STORAGE_ROOT = "guildapi";
    public static final Logger LOGGER = LoggerFactory.getLogger("guildapi");
    public static Gson gson;
    public static JsonObject secrets;
    private static boolean development;

    public static File getModStorageDir(String dirName) {
        return new File(MOD_STORAGE_ROOT, dirName);
    }

    public static boolean isDevelopment() {
        return development;
    }

    @Override
    public void onInitializeClient() {
        // TODO add test command for guild server (/ping?)
        development = FabricLoader.getInstance().isDevelopmentEnvironment();
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


        Managers.Connection.init();
        Managers.Api.init();
        Managers.Feature.init();

    }
}