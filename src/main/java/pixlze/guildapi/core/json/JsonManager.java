package pixlze.guildapi.core.json;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.components.Manager;
import pixlze.guildapi.utils.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class JsonManager extends Manager {
    public final Gson GSON = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().serializeNulls().create();

    public JsonManager() {
        super(List.of());
    }

    @Override
    public void init() {

    }

    public JsonObject loadJsonFromFile(File file) {
        JsonObject configObject = new JsonObject();
        try (FileReader reader = new FileReader(file)) {
            JsonElement jsonElement = JsonParser.parseReader(reader);
            if (jsonElement.isJsonObject()) {
                configObject = jsonElement.getAsJsonObject();
            } else {
                GuildApi.LOGGER.warn("config is not a json object");
            }
        } catch (IOException e) {
            GuildApi.LOGGER.error("json load error: {} {}", e, e.getMessage());
        }
        return configObject;
    }

    public boolean saveJsonAsFile(File file, JsonObject config) {
        FileUtils.mkdir(file.getParentFile());
        try (FileWriter writer = new FileWriter(file)) {
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.jsonValue(config.toString());
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
