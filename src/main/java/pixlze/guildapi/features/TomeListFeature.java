package pixlze.guildapi.features;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.mc.event.WynnChatMessageEvents;
import pixlze.guildapi.net.GuildApiManager;
import pixlze.guildapi.utils.ChatUtils;
import pixlze.guildapi.utils.McUtils;

import java.util.List;
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

    private void listTomes(int amount) {
        new Thread(() -> {
            JsonElement response = Managers.Api.getApi("guild", GuildApiManager.class).get("tomes");
            if (response == null) return;
            List<JsonElement> tomes = response.getAsJsonArray().asList();
            MutableText tomesMessage = Text.literal("Tome list:\n")
                    .setStyle(Style.EMPTY.withColor(Formatting.WHITE));
            for (int i = 0; i < amount; i++) {
                if (i >= tomes.size()) {
                    McUtils.sendLocalMessage(Text.literal("Specified amount exceeded tome list size")
                            .setStyle(Style.EMPTY.withColor(Formatting.YELLOW)));
                    break;
                }
                tomesMessage.append(Text.literal(i + 1 + ". " + tomes.get(i).getAsJsonObject().get("username")
                        .getAsString()).setStyle(Style.EMPTY.withColor(Formatting.WHITE)));
                if (i != Math.min(amount, tomes.size()) - 1) {
                    tomesMessage.append(Text.literal("\n"));
                }
            }
            McUtils.sendLocalMessage(tomesMessage);
        }).start();
    }

    @Override
    public void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("tomelistAdd").executes((context) -> {
                        Managers.Api.getApi("guild", GuildApiManager.class)
                                .post("tomes",
                                        GuildApi.gson.fromJson("{\"username\":\"" + McUtils.playerName() + "\"}",
                                                JsonObject.class), true);
                        return 0;
                    })
            );
            dispatcher.register(ClientCommandManager.literal("tomelist").executes(context -> {
                        listTomes(5);
                        return 0;
                    })
                    .then(ClientCommandManager.argument("amount", IntegerArgumentType.integer(1, 20))
                            .executes(context -> {
                                int amount = IntegerArgumentType.getInteger(context, "amount");
                                listTomes(amount);
                                return 0;
                            })));
        });
        WynnChatMessageEvents.CHAT.register(this::onWynnMessage);
    }
}
