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
import pixlze.guildapi.net.SocketIOManager;
import pixlze.guildapi.utils.ChatUtils;
import pixlze.guildapi.utils.McUtils;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AspectListFeature extends Feature {
    @Override
    public void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("aspectlist")
                .executes(context -> {
                    listAspects(0);
                    return 0;
                })
                .then(ClientCommandManager.argument("page", IntegerArgumentType.integer(1, 10))
                        .executes(context -> {
                            int page = IntegerArgumentType.getInteger(context, "page");
                            listAspects(page - 1);
                            return 0;
                        }))));
        WynnChatMessageEvents.CHAT.register(this::onWynnMessage);
    }

    private void listAspects(int page) {
        new Thread(() -> {
            JsonElement response = Managers.Net.getApi("guild", GuildApiManager.class).get("aspects");
            GuildApi.LOGGER.info("{}", response);
            if (response == null) return;
            List<JsonElement> aspects = response.getAsJsonArray().asList();
            MutableText aspectsMessage = Text.literal("Aspect list page " + (page + 1) + ":\n")
                    .setStyle(Style.EMPTY.withColor(Formatting.WHITE));
            for (int i = 5 * page; i < 5 * (page + 1); i++) {
                if (i >= aspects.size())
                    break;
                aspectsMessage.append(Text.literal(i + 1 + ". " + aspects.get(i).getAsJsonObject()
                                .get("username")
                                .getAsString()).append(": ")
                        .append(aspects.get(i).getAsJsonObject().get("aspects").getAsString())
                        .setStyle(Style.EMPTY.withColor(Formatting.WHITE)));
//                aspectsMessage.append(Text.literal(i + 1 + ". player" + (i + 1)).append(": ")
//                        .append(aspects.get(i).getAsJsonObject().get("aspects").getAsString())
//                        .setStyle(Style.EMPTY.withColor(Formatting.WHITE)));
                if (i != Math.min(page, aspects.size()) - 1) {
                    aspectsMessage.append(Text.literal("\n"));
                }
            }
            boolean hasPrev = page > 0;
            boolean hasNext = 5 * (page + 1) < aspects.size();
            aspectsMessage.append("\n");
            aspectsMessage.append(Text.literal("<< Prev")
                            .setStyle(Style.EMPTY.withColor(hasPrev ? Formatting.GREEN:Formatting.GRAY).withBold(true)
                                    .withClickEvent(hasPrev ? new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/aspectlist " + page):null)))
                    .append("          ").append(Text.literal("Next >>")
                            .setStyle(Style.EMPTY.withColor(hasNext ? Formatting.GREEN:Formatting.GRAY).withBold(true)
                                    .withClickEvent(hasNext ? new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/aspectlist " + (page + 2)):null)));
            McUtils.sendLocalMessage(aspectsMessage);
        }).start();
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
//            if (McUtils.playerName().equals(aspectMatcher.group(1))) {
//                JsonObject requestBody = new JsonObject();
//                requestBody.add("users", Managers.Json.toJsonElement(Arrays.toString(new String[]{aspectMatcher.group(2)})));
//                Managers.Net.getApi("guild", GuildApiManager.class).post("aspects", requestBody, false);
//            } else {
//                GuildApi.LOGGER.warn("tried to decrement aspect for {} but user {} does not match giver {}",
//                        aspectMatcher.group(2), McUtils.playerName(), aspectMatcher.group(1));
//        }
        }
    }
}
