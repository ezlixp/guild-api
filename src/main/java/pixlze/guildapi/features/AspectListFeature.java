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

import java.util.Arrays;
import java.util.List;
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
                requestBody.add("users", GuildApi.gson.fromJson(Arrays.toString(new String[]{aspectMatcher.group(2)}),
                        JsonElement.class));
                Managers.Api.getApi("guild", GuildApiManager.class).post("aspects", requestBody, false);
            } else {
                GuildApi.LOGGER.warn("tried to decrement aspect for {} but user {} does not match giver {}",
                        aspectMatcher.group(2), McUtils.playerName(), aspectMatcher.group(1));
            }
        }
    }

    private void listAspects(int amount) {
        new Thread(() -> {
            JsonElement response = Managers.Api.getApi("guild", GuildApiManager.class).get("aspects");
            if (response == null) return;
            List<JsonElement> aspects = response.getAsJsonArray().asList();
            MutableText aspectsMessage = Text.literal("Aspect list:\n")
                    .setStyle(Style.EMPTY.withColor(Formatting.WHITE));
            for (int i = 0; i < amount; i++) {
                if (i >= aspects.size()) {
                    McUtils.sendLocalMessage(Text.literal("Specified amount exceeded aspect list size")
                            .setStyle(Style.EMPTY.withColor(Formatting.YELLOW)));
                    break;
                }
                aspectsMessage.append(Text.literal(i + 1 + ". " + aspects.get(i).getAsJsonObject()
                                .get("username")
                                .getAsString()).append(": ")
                        .append(aspects.get(i).getAsJsonObject().get("aspects").getAsString())
                        .setStyle(Style.EMPTY.withColor(Formatting.WHITE)));
                if (i != Math.min(amount, aspects.size()) - 1) {
                    aspectsMessage.append(Text.literal("\n"));
                }
            }
            McUtils.sendLocalMessage(aspectsMessage);
        }).start();
    }

    @Override
    public void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("aspectlist")
                .executes(context -> {
                    listAspects(5);
                    return 0;
                })
                .then(ClientCommandManager.argument("amount", IntegerArgumentType.integer(1, 10))
                        .executes(context -> {
                            int amount = IntegerArgumentType.getInteger(context, "amount");
                            listAspects(amount);
                            return 0;
                        }))));
        WynnChatMessageEvents.CHAT.register(this::onWynnMessage);
    }

}
