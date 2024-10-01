package pixlze.guildapi;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pixlze.guildapi.components.Handlers;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.components.Models;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;


public class GuildApi implements ClientModInitializer {
    public static final String MOD_ID = "guildapi";
    public static final String MOD_STORAGE_ROOT = "guildapi";
    public static final Logger LOGGER = LoggerFactory.getLogger("guildapi");
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

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("secrets.json");
        if (inputStream == null) {
            GuildApi.LOGGER.error("secret variables could not be loaded");
        } else {
            secrets = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
        }


        Handlers.Chat.init();
        Managers.Connection.init();
        Managers.Net.init();
        Managers.Feature.init();
        Models.WorldState.init();
    }
}