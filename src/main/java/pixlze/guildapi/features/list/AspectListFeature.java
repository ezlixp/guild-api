package pixlze.guildapi.features.list;

import com.google.gson.JsonElement;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.handlers.chat.event.ChatMessageReceived;
import pixlze.guildapi.net.GuildApiClient;
import pixlze.guildapi.net.SocketIOClient;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.TextUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
        ChatMessageReceived.EVENT.register(this::onWynnMessage);
        super.registerCommands(List.of(ClientCommandManager.literal("search").executes((context) -> {
                    search(McUtils.playerName());
                    return 0;
                })
                .then(ClientCommandManager.argument("username", StringArgumentType.word())
                        .executes((context) -> {
                            search(StringArgumentType.getString(context, "username"));
                            return 0;
                        })
                )));

    }

    private void onWynnMessage(Text message) {
        if (!MinecraftClient.getInstance().isOnThread()) {
            GuildApi.LOGGER.info("not render thread message");
            return;
        }
        String aspectMessage = TextUtils.parsePlain(message);
        Matcher aspectMatcher = Pattern.compile("^ (.*?) rewarded an Aspect to (.*)$").matcher(aspectMessage);
        if (aspectMatcher.find()) {
            GuildApi.LOGGER.info("{} gave an aspect to {}", aspectMatcher.group(1), aspectMatcher.group(2));
            Managers.Net.getApi("socket", SocketIOClient.class)
                    .aspectEmit("give_aspect", Collections.singletonMap("player", aspectMatcher.group(2)));
        }
    }

    private void search(String username) {
        CompletableFuture<JsonElement> response = Managers.Net.getApi("guild", GuildApiClient.class)
                .get("aspects/" + username);
        response.whenCompleteAsync((res, exception) -> {
            if (exception == null && res != null) {
                McUtils.sendLocalMessage(Text.literal(res.getAsJsonObject()
                        .get("username").getAsString() + " is owed " + res.getAsJsonObject()
                        .get("aspects").getAsString() + " aspects.").withColor(0xFFFFFF));
            }
        });
    }
}
