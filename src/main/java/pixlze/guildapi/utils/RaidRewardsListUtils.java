package pixlze.guildapi.utils;

import com.google.gson.JsonElement;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

import java.util.List;

public class RaidRewardsListUtils {
    public static MutableText formatLine(JsonElement listItem, String sortMember) {
        List<Pair<MutableText, String>> components = new java.util.ArrayList<>(List.of(
                new Pair<>(Text.literal(listItem.getAsJsonObject().get("raids").getAsString()).append(" raids"), "raids"),
                new Pair<>(Text.literal(listItem.getAsJsonObject().get("aspects").getAsString()).append(" aspects"), "aspects"),
                new Pair<>(Text.literal(String.format("%.2f", listItem.getAsJsonObject().get("liquidEmeralds").getAsDouble())).append(" ¼²"), "liquidEmeralds")
        ));
        components.sort((a, b) -> {
            if (a.getRight().equals(sortMember)) return -1;
            if (b.getRight().equals(sortMember)) return 1;
            return 0;
        });
        MutableText out = Text.literal(listItem.getAsJsonObject().get("mcUsername")
                .getAsString()).append(": ");
        for (int i = 0; i < components.size() - 1; i++) {
            out.append(components.get(i).getLeft()).append(" | ");
        }
        out.append(components.getLast().getLeft());
        return out;
    }
}
