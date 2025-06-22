package pixlze.guildapi;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pixlze.guildapi.core.components.Handlers;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.models.Models;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;


public class GuildApi implements ClientModInitializer {
    public static final String MOD_ID = "guildapi";
    public static final String MOD_STORAGE_ROOT = "guildapi";
    public static final Logger LOGGER = LoggerFactory.getLogger("guildapi");
    public static final Text BASE_INFO = Text.literal("§a§lGuild API §r§av\" + MOD_VERSION + \" by §lpixlze§r§a.\\n§fType /guildapi help for a list of commands.\"");
    public static ModContainer MOD_CONTAINER;
    public static String MOD_VERSION;
    public static JsonObject secrets;
    private static boolean development;

    public static File getModStorageDir(String dirName) {
        return new File(MOD_STORAGE_ROOT, dirName);
    }

    public static boolean isDevelopment() {
        return development;
    }

    public static boolean isTesting() {
        return true;
    }

    @Override
    public void onInitializeClient() {
        System.setProperty("java.awt.headless", "false");
        development = FabricLoader.getInstance().isDevelopmentEnvironment();
        if (FabricLoader.getInstance().getModContainer(MOD_ID).isPresent()) {
            MOD_CONTAINER = FabricLoader.getInstance().getModContainer(MOD_ID).get();
            MOD_VERSION = MOD_CONTAINER.getMetadata().getVersion().getFriendlyString();
        }

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("secrets.json");
        if (inputStream == null) {
            GuildApi.LOGGER.error("secret variables could not be loaded");
        } else {
            secrets = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
        }


        Handlers.init();
        Managers.init();
        Models.WorldState.init();
    }
}