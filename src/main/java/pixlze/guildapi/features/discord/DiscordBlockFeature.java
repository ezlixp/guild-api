package pixlze.guildapi.features.discord;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.components.Models;
import pixlze.guildapi.features.type.ListFeature;
import pixlze.guildapi.net.GuildApiClient;
import pixlze.guildapi.net.event.NetEvents;
import pixlze.guildapi.net.type.Api;
import pixlze.guildapi.utils.JsonUtils;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.type.Prepend;

import java.util.HashSet;

public class DiscordBlockFeature extends ListFeature {
    private final GuildApiClient guildApiClient = Managers.Net.guild;

    public DiscordBlockFeature() {
        super("block", "user/blocked/" + McUtils.playerUUID(), (listItem) -> Text.literal(listItem.getAsString()));
    }

    @Override
    public void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(GuildApi.BASE_COMMAND.then(ClientCommandManager.literal("block")
                    .then(ClientCommandManager.literal("add").then(ClientCommandManager.argument("toBlock", StringArgumentType.word()).executes((context) -> {
                        String toBlock = StringArgumentType.getString(context, "toBlock");
                        guildApiClient.post("user/blocked/" + McUtils.player()
                                .getUuidAsString(), JsonUtils.toJsonObject("{toBlock:\"" + toBlock + "\"}"), true);
                        Models.DiscordMessage.block(toBlock);
                        return Command.SINGLE_SUCCESS;
                    }))).then(
                            ClientCommandManager.literal("list").executes((context) -> {
                                MutableText out = Text.literal("§aBlocked usernames:");
                                HashSet<String> blocked = Models.DiscordMessage.getBlocked();
                                for (String block : blocked) out.append(block);
                                McUtils.sendLocalMessage(out, Prepend.GUILD.getWithStyle(Style.EMPTY.withColor(Formatting.GREEN)), true);
                                return Command.SINGLE_SUCCESS;
                            })
                    )));
        });
        NetEvents.LOADED.register(this::onApiLoaded);
    }

    private void onApiLoaded(Api api) {
        if (api.getClass().equals(GuildApiClient.class)) {
            guildApiClient.get("user/blocked/" + McUtils.playerUUID()).whenCompleteAsync((res, error) -> {
                if (error != null) {
                    GuildApi.LOGGER.error("get blocked error: {} {}", error.getMessage(), error.getMessage());
                }
                GuildApi.LOGGER.info("{} blocked peoploe:", res);
            });
        }
    }
}
