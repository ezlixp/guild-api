package pixlze.mod.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import net.minecraft.client.gui.screen.Screen;
import pixlze.mod.PixUtils;
import pixlze.mod.config.types.Option;
import pixlze.mod.config.types.SubConfig;
import pixlze.mod.config.types.Toggle;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class PixUtilsConfig {
    //    @JsonAdapter(OptionsAdapter.class)
    private static final ArrayList<Option> options = new ArrayList<>();
    public static JsonObject configObject;

    public static void initialize() {
        try (FileReader reader = new FileReader(new File(PixUtils.MOD_ID + "/config", "config.json"))) {
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

    public static Toggle registerToggle(String name, boolean value) {
        Toggle registeredToggle = new Toggle(name, value);
        options.add(registeredToggle);
        return registeredToggle;
    }

    public static Toggle registerToggle(String name, boolean value, ArrayList<Option> children) {
        Toggle registeredToggle = new Toggle(name, value, children);
        options.add(registeredToggle);
        return registeredToggle;
    }

    public static <T> SubConfig<T> registerSubConfig(String name, String buttonText, Screen subConfigScreen, ArrayList<T> value) {
        SubConfig<T> registeredSubConfig = new SubConfig<T>(name, buttonText, subConfigScreen, value);
        options.add(registeredSubConfig);
        return registeredSubConfig;
    }

    public static ArrayList<Option> getOptions() {
        return new ArrayList<>(options);
    }

    public static void save() throws IOException {
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
}
