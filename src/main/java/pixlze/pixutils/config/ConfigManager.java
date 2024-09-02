package pixlze.pixutils.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import net.minecraft.client.gui.screen.Screen;
import pixlze.pixutils.PixUtils;
import pixlze.pixutils.config.types.Option;
import pixlze.pixutils.config.types.SubConfig;
import pixlze.pixutils.config.types.Toggle;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ConfigManager {
    private final ArrayList<Option> options = new ArrayList<>();
    private final File configFile;
    public JsonObject configObject;

    public ConfigManager() {
        configFile = new File(PixUtils.MOD_ID + "/config", "config.json");

    }

    public void init() {
        try (FileReader reader = new FileReader(configFile)) {
            JsonElement jsonElement = JsonParser.parseReader(reader);
            if (jsonElement.isJsonObject()) {
                configObject = jsonElement.getAsJsonObject();
            } else {
                PixUtils.LOGGER.error("config is not a json object");
            }
        } catch (IOException e) {
            PixUtils.LOGGER.error(e.getMessage());
        }
    }

    public ArrayList<Option> getOptions() {
        return new ArrayList<>(options);
    }

    public void save() throws IOException {
        File file = new File(PixUtils.MOD_ID + "/config", "config.json");
        boolean madeFile = file.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(file)) {
            JsonWriter jsonWriter = new JsonWriter(writer);
            configObject = new JsonObject();
            for (Option option : options) {
                PixUtils.gson.toJson(option);
            }
            jsonWriter.jsonValue(configObject.toString());
        } catch (Exception e) {
            PixUtils.LOGGER.error(e.getMessage());
        }
    }

    public Toggle registerToggle(String name, String id, boolean value) {
        Toggle registeredToggle = new Toggle(name, id, value);
        options.add(registeredToggle);
        return registeredToggle;
    }

    public Toggle registerToggle(String name, String id, boolean value, ArrayList<Option> children) {
        Toggle registeredToggle = new Toggle(name, id, value, children);
        options.add(registeredToggle);
        return registeredToggle;
    }

    public <T> SubConfig<T> registerSubConfig(String name, String id, String buttonText, Screen subConfigScreen, ArrayList<T> value) {
        SubConfig<T> registeredSubConfig = new SubConfig<>(name, id, buttonText, subConfigScreen, value);
        options.add(registeredSubConfig);
        return registeredSubConfig;
    }
}
