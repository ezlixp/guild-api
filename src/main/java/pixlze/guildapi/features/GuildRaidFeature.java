package pixlze.guildapi.features;

import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.mc.event.WynnChatMessageEvents;
import pixlze.guildapi.net.GuildApiClient;
import pixlze.guildapi.utils.ChatUtils;
import pixlze.guildapi.utils.McUtils;

import java.time.Instant;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuildRaidFeature extends Feature {
    @Override
    public void init() {
        WynnChatMessageEvents.CHAT.register(this::onWynnMessage);
    }

    private void onWynnMessage(Text message) {
        if (!MinecraftClient.getInstance().isOnThread()) {
            GuildApi.LOGGER.info("not render thread message");
            return;
        }
        String raidMessage = ChatUtils.parseRaid(message);
        Matcher raidMatcher = Pattern.compile(".*§e(.*?)§b.*§e(.*?)§b.*§e(.*?)§b.*§e(.*?)§b.*?§3(.*?)§b")
                .matcher(raidMessage);
//        Matcher raidMatcher = Pattern.compile(".*&e(.*?)&b.*&e(.*?)&b.*&e(.*?)&b.*&e(.*?)&b.*?&3(.*?)&b").matcher
//        (raidMessage);
        if (raidMatcher.find() && !raidMessage.contains(":")) {
            GuildApi.LOGGER.info("guild raid {} finished", raidMatcher.group(5));
            McUtils.sendLocalMessage(Text.literal("Guild raid finished.").withColor(0x00FF00));
            JsonObject requestBody = new JsonObject();
            requestBody.add("users", Managers.Json.toJsonElement(Arrays.toString(new String[]{raidMatcher.group(1), raidMatcher.group(2), raidMatcher.group(3), raidMatcher.group(4)})));
            requestBody.addProperty("raid", raidMatcher.group(5));
            requestBody.addProperty("timestamp", Instant.now().toEpochMilli());
            Managers.Net.getApi("guild", GuildApiClient.class).post("addRaid", requestBody, false);
        }
    }
}
