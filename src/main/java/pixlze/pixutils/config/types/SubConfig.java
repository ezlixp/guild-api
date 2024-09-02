package pixlze.pixutils.config.types;

import com.google.gson.JsonArray;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import pixlze.pixutils.PixUtils;
import pixlze.pixutils.components.Managers;
import pixlze.pixutils.features.chat_notifications.EditNotificationsScreen;

import java.io.IOException;
import java.util.ArrayList;


@JsonAdapter(SubConfig.SubConfigAdapter.class)
public class SubConfig<T> extends Option {
    public final String buttonText;
    private final ArrayList<T> value;
    private final Screen open;

    public SubConfig(String name, String id, String buttonText, Screen open, ArrayList<T> value) {
        super(name, id, "SubConfig");
        this.name = name;
        this.buttonText = buttonText;
        this.open = open;
        this.value = value;
    }

    public void click() {
        ((EditNotificationsScreen) open).open(MinecraftClient.getInstance().currentScreen);
    }

    public ArrayList<T> getValue() {
        return value;
    }

    static class SubConfigAdapter<T> extends TypeAdapter<SubConfig<T>> {

        @Override
        public void write(JsonWriter out, SubConfig<T> value) throws IOException {
            JsonArray config = new JsonArray();
            for (T item : value.value) {
                config.add(PixUtils.gson.toJsonTree(item));
            }
            Managers.Config.configObject.add(value.id, config);
        }

        @Override
        public SubConfig<T> read(JsonReader in) throws IOException {
            return null;
        }
    }
}
