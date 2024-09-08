package pixlze.guildapi.features;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.mc.event.WynnChatMessageEvents;
import pixlze.guildapi.net.GuildApiManager;
import pixlze.guildapi.utils.ChatUtils;
import pixlze.guildapi.utils.McUtils;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AspectListFeature extends Feature {
    private void onWynnMessage(Text message) {
        if (!MinecraftClient.getInstance().isOnThread()) {
            GuildApi.LOGGER.info("not render thread message");
            return;
        }
        String aspectMessage = ChatUtils.parsePlain(message);
        Matcher aspectMatcher = Pattern.compile("^ (.*?) rewarded an Aspect to (.*)$").matcher(aspectMessage);
        if (aspectMatcher.find()) {
            GuildApi.LOGGER.info("{} gave an aspect to {}", aspectMatcher.group(1), aspectMatcher.group(2));
            if (McUtils.playerName().equals(aspectMatcher.group(1))) {
                JsonObject requestBody = new JsonObject();
                requestBody.add("users", GuildApi.gson.fromJson(Arrays.toString(new String[]{aspectMatcher.group(2)}), JsonElement.class));
                Managers.Api.getApi("guild", GuildApiManager.class).post("aspects", requestBody, false);
            } else {
                GuildApi.LOGGER.warn("tried to decrement aspect for {} but user {} does not match giver {}", aspectMatcher.group(2), McUtils.playerName(), aspectMatcher.group(1));
            }
        }
    }

    @Override
    public void init() {
        WynnChatMessageEvents.CHAT.register(this::onWynnMessage);
    }

}
