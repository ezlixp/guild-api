package pixlze.guildapi.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonManager {
    public static final Gson GSON = new GsonBuilder().create();

    public JsonElement toJsonElement(String convert) {
        return GSON.fromJson(convert, JsonElement.class);
    }

    public JsonObject toJsonObject(String convert) {
        return GSON.fromJson(convert, JsonObject.class);
    }

}
