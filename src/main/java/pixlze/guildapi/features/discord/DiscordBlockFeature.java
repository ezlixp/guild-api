package pixlze.guildapi.features.discord;

import com.google.gson.JsonElement;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.text.Text;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.Managers;
import pixlze.guildapi.features.type.ListFeature;
import pixlze.guildapi.models.Models;
import pixlze.guildapi.net.GuildApiClient;
import pixlze.guildapi.net.event.NetEvents;
import pixlze.guildapi.net.type.Api;
import pixlze.guildapi.utils.ColourUtils;
import pixlze.guildapi.utils.JsonUtils;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.NetUtils;
import pixlze.guildapi.utils.type.Prepend;

import java.util.List;

public class DiscordBlockFeature extends ListFeature {
    private static final String ENDPOINT = "user/blocked/";
    private final GuildApiClient guildApiClient = Managers.Net.guild;


    public DiscordBlockFeature() {
        super("block", ENDPOINT, (listItem) -> {
            Models.DiscordMessage.block(listItem.getAsString());
            return Text.literal(listItem.getAsString());
        });
    }

    @Override
    public void init() {
        super.registerCommands(List.of(
                ClientCommandManager.literal("add").then(ClientCommandManager.argument("toBlock", StringArgumentType.word()).executes((context) -> {
                    String toBlock = StringArgumentType.getString(context, "toBlock");
                    if (Models.DiscordMessage.getBlocked().size() >= 50) {
                        McUtils.sendLocalMessage(Text.literal("§cMy brother in christ why do you want to block more than 50 people."), Prepend.GUILD.getWithStyle(ColourUtils.RED), true);
                        return 0;
                    }
                    if (toBlock.equalsIgnoreCase(McUtils.playerName())) {
                        McUtils.sendLocalMessage(Text.literal("§cYou can't block yourself."), Prepend.GUILD.getWithStyle(ColourUtils.RED), true);
                        return 0;
                    }
                    guildApiClient.post(ENDPOINT + McUtils.playerUUID(), JsonUtils.toJsonObject("{toBlock:\"" + toBlock + "\"}")).whenCompleteAsync((res, exception) -> {
                        try {
                            NetUtils.applyDefaultCallback(res, exception, (resOK) -> {
                                McUtils.sendLocalMessage(Text.literal("§aSuccessfully blocked \"" + toBlock + "\".\n" +
                                        "All purple discord messages from them will be hidden."), Prepend.GUILD.getWithStyle(ColourUtils.GREEN), true);
                                Models.DiscordMessage.block(toBlock);
                            }, (error) -> {
                                if (error.equals("Blocked list full.")) {
                                    McUtils.sendLocalMessage(Text.literal("§cMy brother in christ why do you want to block more than 50 people."), Prepend.GUILD.getWithStyle(ColourUtils.RED), true);
                                } else if (error.equals("User already in block list.")) {
                                    McUtils.sendLocalMessage(Text.literal("§e" + toBlock + " has already been blocked."), Prepend.GUILD.getWithStyle(ColourUtils.YELLOW), true);
                                } else {
                                    McUtils.sendLocalMessage(Text.literal("§cSomething went wrong: " + error), Prepend.DEFAULT.get(), false);
                                    GuildApi.LOGGER.error("block add error: {}", error);
                                }
                            });
                        } catch (Exception e) {
                            NetUtils.defaultException("add blocked", e);
                        }
                    });
                    return Command.SINGLE_SUCCESS;
                })),
                ClientCommandManager.literal("remove").then(ClientCommandManager.argument("toRemove", StringArgumentType.word()).executes((context -> {
                    String toRemove = StringArgumentType.getString(context, "toRemove");
                    guildApiClient.delete(ENDPOINT + McUtils.playerUUID() + "/" + toRemove).whenCompleteAsync((res, exception) -> {
                        try {
                            NetUtils.applyDefaultCallback(res, exception, (resOK) -> {
                                McUtils.sendLocalMessage(Text.literal("§aSuccessfully unblocked " + toRemove + ".\n" +
                                        "Messages from them will be shown again."), Prepend.GUILD.getWithStyle(ColourUtils.GREEN), true);
                                Models.DiscordMessage.unblock(toRemove);
                            }, (error) -> {
                                if (error.equals("Blocked user not found.")) {
                                    McUtils.sendLocalMessage(Text.literal("§e" + toRemove + " was not blocked."), Prepend.GUILD.getWithStyle(ColourUtils.YELLOW), true);
                                }
                            });
                        } catch (Exception e) {
                            NetUtils.defaultException("remove blocked", e);
                        }
                    });
                    return Command.SINGLE_SUCCESS;
                })))
        ));
        NetEvents.LOADED.register(this::onApiLoaded);
    }

    private void onApiLoaded(Api api) {
        if (api.getClass().equals(GuildApiClient.class)) {
            super.endpoint = ENDPOINT;
            setExtra(McUtils.playerUUID());

            guildApiClient.get(ENDPOINT + McUtils.playerUUID()).whenCompleteAsync((res, error) -> {
                try {
                    NetUtils.applyDefaultCallback(res, error, (resOK) -> {
                        List<JsonElement> blocked = resOK.getAsJsonArray().asList();
                        for (JsonElement block : blocked) {
                            GuildApi.LOGGER.info("blocking: {}", block.getAsString());
                            Models.DiscordMessage.block(block.getAsString());
                        }
                    }, NetUtils.defaultFailed("Blocked users fetch", true));
                } catch (Exception e) {
                    McUtils.sendLocalMessage(Text.literal("§cSomething went wrong. Check logs for more details."), Prepend.DEFAULT.get(), false);
                    GuildApi.LOGGER.error("blocked users fetch error: {} {}", e, e.getMessage());
                }
            });
        }
    }
}
