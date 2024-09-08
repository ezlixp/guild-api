package pixlze.guildapi.features;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.mc.event.WynnChatMessageEvents;
import pixlze.guildapi.net.GuildApiManager;
import pixlze.guildapi.utils.ChatUtils;
import pixlze.guildapi.utils.McUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TomeListFeature extends Feature {
    private void onWynnMessage(Text message) {
        if (!MinecraftClient.getInstance().isOnThread()) {
            GuildApi.LOGGER.info("not render thread message");
            return;
        }
        String tomeMessage = ChatUtils.parsePlain(message);
        Matcher tomeMatcher = Pattern.compile("^ (.*?) rewarded a Guild Tome to (.*)$").matcher(tomeMessage);
        if (tomeMatcher.find()) {
            GuildApi.LOGGER.info("{} gave a tome to {}", tomeMatcher.group(1), tomeMatcher.group(2));
            Managers.Api.getApi("guild", GuildApiManager.class).delete("tomes/" + tomeMatcher.group(2), false);
        }
    }

    @Override
    public void init() {
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("tomelist").executes((context) -> {
            Managers.Api.getApi("guild", GuildApiManager.class).post("tomes", GuildApi.gson.fromJson("{\"username\":\"" + McUtils.playerName() + "\"}", JsonObject.class), true);

            return 0;
        }))));
        WynnChatMessageEvents.CHAT.register(this::onWynnMessage);
    }

}
