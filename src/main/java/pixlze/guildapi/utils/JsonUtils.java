package pixlze.guildapi.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonUtils {
    public static final Gson GSON = new GsonBuilder().create();

    public static JsonElement toJsonElement(String convert) {
        return GSON.fromJson(convert, JsonElement.class);
    }

    public static JsonObject toJsonObject(String convert) {
        return GSON.fromJson(convert, JsonObject.class);
    }

}
