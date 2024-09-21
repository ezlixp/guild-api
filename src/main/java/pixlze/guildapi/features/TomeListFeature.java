package pixlze.guildapi.features;

import com.google.gson.JsonElement;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
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
    @Override
    public void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("tomelistAdd").executes((context) -> {
                        Managers.Net.getApi("guild", GuildApiManager.class)
                                .post("tomes", Managers.Json.toJsonObject("{\"username\":\"" + McUtils.playerName() + "\"}"), true);
                        return 0;
                    })
            );
            dispatcher.register(ClientCommandManager.literal("tomelist").executes(context -> {
                        listTomes(0);
                        return 0;
                    })
                    .then(ClientCommandManager.argument("page", IntegerArgumentType.integer(1, 20))
                            .executes(context -> {
                                int page = IntegerArgumentType.getInteger(context, "page");
                                listTomes(page - 1);
                                return 0;
                            })));
        });
        WynnChatMessageEvents.CHAT.register(this::onWynnMessage);
    }

    private void listTomes(int page) {
        new Thread(() -> {
            JsonElement response = Managers.Net.getApi("guild", GuildApiManager.class).get("tomes");
            if (response == null) return;
            List<JsonElement> tomes = response.getAsJsonArray().asList();
            MutableText tomesMessage = Text.literal("Tome list page " + (page + 1) + ":\n")
                    .setStyle(Style.EMPTY.withColor(Formatting.WHITE));
            for (int i = 5 * page; i < 5 * (page + 1); i++) {
                if (i >= tomes.size())
                    break;
                tomesMessage.append(Text.literal(i + 1 + ". " + tomes.get(i).getAsJsonObject().get("username")
                        .getAsString()).setStyle(Style.EMPTY.withColor(Formatting.WHITE)));

//                tomesMessage.append(Text.literal(i + 1 + ". player" + (i + 1))
//                        .setStyle(Style.EMPTY.withColor(Formatting.WHITE)));
                if (i != Math.min(page, tomes.size()) - 1) {
                    tomesMessage.append(Text.literal("\n"));
                }
            }
            boolean hasPrev = page > 0;
            boolean hasNext = 5 * (page + 1) < tomes.size();
            tomesMessage.append("\n");
            tomesMessage.append(Text.literal("<< Prev")
                            .setStyle(Style.EMPTY.withColor(hasPrev ? Formatting.GREEN:Formatting.GRAY).withBold(true)
                                    .withClickEvent(hasPrev ? new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tomelist " + page):null)))
                    .append("          ").append(Text.literal("Next >>")
                            .setStyle(Style.EMPTY.withColor(hasNext ? Formatting.GREEN:Formatting.GRAY).withBold(true)
                                    .withClickEvent(hasNext ? new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tomelist " + (page + 2)):null)));
            McUtils.sendLocalMessage(tomesMessage);
        }).start();
    }

    private void onWynnMessage(Text message) {
        if (!MinecraftClient.getInstance().isOnThread()) {
            GuildApi.LOGGER.info("not render thread message");
            return;
        }
        String tomeMessage = ChatUtils.parsePlain(message);
        Matcher tomeMatcher = Pattern.compile("^ (.*?) rewarded a Guild Tome to (.*)$").matcher(tomeMessage);
        if (tomeMatcher.find()) {
            GuildApi.LOGGER.info("{} gave a tome to {}", tomeMatcher.group(1), tomeMatcher.group(2));
            Managers.Net.getApi("guild", GuildApiManager.class).delete("tomes/" + tomeMatcher.group(2), false);
        }
    }
}
