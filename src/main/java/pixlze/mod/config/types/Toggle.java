package pixlze.mod.config.types;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import pixlze.mod.config.PixUtilsConfig;

import java.io.IOException;
import java.util.ArrayList;

@JsonAdapter(Toggle.ToggleAdapter.class)
public class Toggle extends Option {
    private boolean value = false;
    private ArrayList<Option> children;

    public Toggle(String name, boolean value) {
        super(name, "Toggle");
        this.value = value;
    }

    public Toggle(String name, boolean value, ArrayList<Option> children) {
        super(name, "Toggle");
        this.value = value;
        this.children = children;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean state) {
        this.value = state;
    }

    public ArrayList<Option> getChildren() {
        return children;
    }


    static class ToggleAdapter extends TypeAdapter<Toggle> {

        @Override
        public void write(JsonWriter out, Toggle value) throws IOException {
            PixUtilsConfig.configObject.addProperty(value.name, value.value);
        }

        @Override
        public Toggle read(JsonReader in) throws IOException {
            return null;
        }
    }

}
