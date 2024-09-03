package pixlze.guildapi.features;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.mc.event.WynnChatMessageEvents;
import pixlze.guildapi.utils.ChatUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AspectListFeature extends Feature {
    private void onWynnMessage(Text message) {
        if (!MinecraftClient.getInstance().isOnThread()) {
            GuildApi.LOGGER.info("not render thread message");
            return;
        }
        String aspectMessage = ChatUtils.parsePlain(message);
        Matcher aspectMatcher = Pattern.compile("^\uDAFF\uDFFC\uE001\uDB00\uDC06 (.*?) rewarded an Aspect to (.*)$").matcher(aspectMessage);
        if (aspectMatcher.find()) {
            GuildApi.LOGGER.info("{} gave an aspect to {}", aspectMatcher.group(1), aspectMatcher.group(2));
        }
    }

    @Override
    public void init() {
        WynnChatMessageEvents.CHAT.register(this::onWynnMessage);
    }

}
