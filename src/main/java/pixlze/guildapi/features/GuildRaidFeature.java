package pixlze.guildapi.features;

import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.handlers.chat.event.ChatMessageReceived;
import pixlze.guildapi.net.GuildApiClient;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.text.TextUtils;
import pixlze.guildapi.utils.text.type.TextParseOptions;
import pixlze.guildapi.utils.type.Prepend;

import java.time.Instant;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuildRaidFeature extends Feature {
    private final Pattern RAID_PATTERN = Pattern.compile("^§[b8]((\uDAFF\uDFFC\uE001\uDB00\uDC06)|(\uDAFF\uDFFC\uE006\uDAFF\uDFFF\uE002\uDAFF\uDFFE))§[b8] §[e8](?<player1>.*?)§[b8], §[e8](?<player2>.*?)§[b8], §[e8](?<player3>.*?)§[b8], and §[e8](?<player4>.*?)§[b8] finished §[38](?<raid>.*?)§[b8].*$");
    @Override
    public void init() {
        ChatMessageReceived.EVENT.register(this::onWynnMessage);
    }

    private void onWynnMessage(Text message) {
        if (!MinecraftClient.getInstance().isOnThread()) {
            GuildApi.LOGGER.info("not render thread message");
            return;
        }
        Matcher raidMatcher = RAID_PATTERN.matcher(TextUtils.parseStyled(message, TextParseOptions.DEFAULT.withExtractUsernames(true)));
        if (raidMatcher.find()) {
            GuildApi.LOGGER.info("guild raid {} finished", raidMatcher.group("raid"));
            McUtils.sendLocalMessage(Text.literal("Guild raid finished.").withColor(0x00FF00), Prepend.DEFAULT);
            JsonObject requestBody = new JsonObject();
            requestBody.add("users", Managers.Json.toJsonElement(Arrays.toString(new String[]{raidMatcher.group("player1"), raidMatcher.group("player2"), raidMatcher.group("player3"), raidMatcher.group("player4")})));
            requestBody.addProperty("raid", raidMatcher.group("raid"));
            requestBody.addProperty("timestamp", Instant.now().toEpochMilli());
            Managers.Net.getApi("guild", GuildApiClient.class).post("addRaid", requestBody, false);
        }
    }
}
