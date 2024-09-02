package pixlze.guildapi.json.type_adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.util.Pair;
import pixlze.guildapi.GuildApi;

import java.io.IOException;

public class PairAdapter extends TypeAdapter<Pair<?, ?>> {
    @Override
    public void write(JsonWriter out, Pair<?, ?> value) throws IOException {
        out.beginObject();
        out.name("left");
        GuildApi.gson.toJson(value.getLeft(), value.getLeft().getClass(), out);
        out.name("right");
        GuildApi.gson.toJson(value.getRight(), value.getRight().getClass(), out);
        out.endObject();
    }

    @Override
    public Pair<?, ?> read(JsonReader in) throws IOException {
        return null;
    }
}
