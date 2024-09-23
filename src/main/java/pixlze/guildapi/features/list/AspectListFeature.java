package pixlze.guildapi.features.list;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.mc.event.WynnChatMessageEvents;
import pixlze.guildapi.net.SocketIOManager;
import pixlze.guildapi.utils.ChatUtils;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AspectListFeature extends ListFeature {
    public AspectListFeature() {
        super("aspect", "aspects", (listItem) -> Text.literal(listItem.get("username")
                        .getAsString()).append(": ")
                .append(listItem.get("aspects").getAsString())
                .setStyle(Style.EMPTY.withColor(Formatting.WHITE)));
    }

    @Override
    public void init() {
        WynnChatMessageEvents.CHAT.register(this::onWynnMessage);
        super.init();

    }

    private void onWynnMessage(Text message) {
        if (!MinecraftClient.getInstance().isOnThread()) {
            GuildApi.LOGGER.info("not render thread message");
            return;
        }
        String aspectMessage = ChatUtils.parsePlain(message);
        Matcher aspectMatcher = Pattern.compile("^ (.*?) rewarded an Aspect to (.*)$").matcher(aspectMessage);
        if (aspectMatcher.find()) {
            GuildApi.LOGGER.info("{} gave an aspect to {}", aspectMatcher.group(1), aspectMatcher.group(2));
            Managers.Net.getApi("socket", SocketIOManager.class)
                    .emitEvent("give_aspect", Collections.singletonMap("player", aspectMatcher.group(2)));
        }
    }
}
