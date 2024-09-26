package pixlze.guildapi.features.list;

import com.google.gson.JsonElement;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.handlers.chat.event.ChatMessageReceived;
import pixlze.guildapi.net.GuildApiClient;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.TextUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TomeListFeature extends ListFeature {
    public TomeListFeature() {
        super("tome", "tomes", (listItem) ->
                Text.literal(listItem.get("username")
                        .getAsString()).setStyle(Style.EMPTY.withColor(Formatting.WHITE)));
    }

    @Override
    public void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("tomelist")
                .then(ClientCommandManager.literal("add"))
                .executes(context -> {
                    Managers.Net.getApi("guild", GuildApiClient.class)
                            .post("tomes", Managers.Json.toJsonObject("{\"username\":\"" + McUtils.playerName() + "\"}"), true);
                    return 0;
                }))
        );
        ChatMessageReceived.EVENT.register(this::onWynnMessage);
        super.registerCommands(
                List.of(ClientCommandManager.literal("add").executes((context) -> {
                                    Managers.Net.getApi("guild", GuildApiClient.class)
                                            .post("tomes", Managers.Json.toJsonObject("{\"username\":\"" + McUtils.playerName() + "\"}"), true);
                                    return 0;

                                }
                        ), ClientCommandManager.literal("search").executes((context) -> {
                                    search(McUtils.playerName());
                                    return 0;
                                })
                                .then(ClientCommandManager.argument("username", StringArgumentType.word())
                                        .executes((context) -> {
                                            search(StringArgumentType.getString(context, "username"));
                                            return 0;
                                        })
                                )
                )
        );
    }

    private void onWynnMessage(Text message) {
        if (!MinecraftClient.getInstance().isOnThread()) {
            GuildApi.LOGGER.info("not render thread message");
            return;
        }
        String tomeMessage = TextUtils.parsePlain(message);
        Matcher tomeMatcher = Pattern.compile("^ (.*?) rewarded a Guild Tome to (.*)$").matcher(tomeMessage);
        if (tomeMatcher.find()) {
            GuildApi.LOGGER.info("{} gave a tome to {}", tomeMatcher.group(1), tomeMatcher.group(2));
            Managers.Net.getApi("guild", GuildApiClient.class).delete("tomes/" + tomeMatcher.group(2), false);
        }
    }

    private void search(String username) {
        CompletableFuture<JsonElement> response = Managers.Net.getApi("guild", GuildApiClient.class)
                .get("tomes/" + username);
        response.whenCompleteAsync((res, exception) -> {
            if (exception == null && res != null) {
                McUtils.sendLocalMessage(Text.literal(res.getAsJsonObject()
                        .get("username")
                        .getAsString() + " is at position " + res
                        .getAsJsonObject()
                        .get("position").getAsString() + ".").withColor(0xFFFFFF));
            }
        });
    }
}
