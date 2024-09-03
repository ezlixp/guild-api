package pixlze.guildapi.features;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.mc.event.WynnChatMessageEvents;
import pixlze.guildapi.utils.ChatUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TomeListFeature extends Feature {
    private void onWynnMessage(Text message) {
        if (!MinecraftClient.getInstance().isOnThread()) {
            GuildApi.LOGGER.info("not render thread message");
            return;
        }
        String tomeMessage = ChatUtils.parsePlain(message);
        Matcher tomeMatcher = Pattern.compile("^\uDAFF\uDFFC\uE001\uDB00\uDC06 (.*?) rewarded a Tome to (.*)$").matcher(tomeMessage);
        if (tomeMatcher.find()) {
            GuildApi.LOGGER.info("{} gave a tome to {}", tomeMatcher.group(1), tomeMatcher.group(2));
        }
    }

    @Override
    public void init() {
        WynnChatMessageEvents.CHAT.register(this::onWynnMessage);
    }

}
