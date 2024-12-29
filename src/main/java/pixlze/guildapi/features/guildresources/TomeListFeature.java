package pixlze.guildapi.features.guildresources;

import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.features.type.ListFeature;
import pixlze.guildapi.handlers.chat.event.ChatMessageReceived;
import pixlze.guildapi.mc.event.WynnChatMessage;
import pixlze.guildapi.utils.JsonUtils;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.NetUtils;
import pixlze.guildapi.utils.text.TextUtils;
import pixlze.guildapi.utils.text.type.TextParseOptions;
import pixlze.guildapi.utils.type.Prepend;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TomeListFeature extends ListFeature {
    private static final Pattern TOME_MESSAGE_PATTERN = Pattern.compile("^§.((\uDAFF\uDFFC\uE001\uDB00\uDC06)|(\uDAFF\uDFFC\uE006\uDAFF\uDFFF\uE002\uDAFF\uDFFE))§. §.(?<giver>.*?)(§.)? rewarded §.a Guild Tome§. to §.(?<receiver>.*?)(§.)?$");
    private static final String ENDPOINT = "guilds/tomes/";

    public TomeListFeature() {
        super("tome", ENDPOINT, (listItem) -> Text.literal(listItem.getAsJsonObject().get("username").getAsString()).setStyle(Style.EMPTY.withColor(Formatting.WHITE)));
    }

    @Override
    public void init() {
        ChatMessageReceived.EVENT.register(this::onWynnMessage);
        super.registerCommands(List.of(ClientCommandManager.literal("add").executes((context) -> {
            Managers.Net.guild.post(ENDPOINT + Managers.Net.guild.guildId, JsonUtils.toJsonObject("{\"username\":\"" + McUtils.playerName() + "\"}")).whenCompleteAsync((res, exception) -> {
                try {
                    NetUtils.applyDefaultCallback(res, exception, (response) -> McUtils.sendLocalMessage(Text.literal("§aSuccessfully added to the tome queue"), Prepend.DEFAULT.get(), false),
                            (error) -> {
                                if (error.equals("User already in tome list.")) {
                                    McUtils.sendLocalMessage(Text.literal("§eYou are already in the tome list. Wait until you receive a tome to re-add yourself."), Prepend.DEFAULT.get(), false);
                                } else {
                                    McUtils.sendLocalMessage(Text.literal("§cCould not add to tome list. Reason: " + error), Prepend.DEFAULT.get(), false);
                                }
                            });
                } catch (Exception e) {
                    McUtils.sendLocalMessage(Text.literal("§cSomething went wrong"), Prepend.DEFAULT.get(), false);
                    GuildApi.LOGGER.error("tomelist add error: {} {}", e, e.getMessage());
                }
            });
            return Command.SINGLE_SUCCESS;

        }), ClientCommandManager.literal("search").executes((context) -> {
            search(McUtils.playerName());
            return Command.SINGLE_SUCCESS;
        }).then(ClientCommandManager.argument("username", StringArgumentType.word()).executes((context) -> {
            search(StringArgumentType.getString(context, "username"));
            return Command.SINGLE_SUCCESS;
        }))));

        if (GuildApi.isDevelopment()) {
            ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
                dispatcher.register(ClientCommandManager.literal("tome").then(ClientCommandManager.argument("username", StringArgumentType.word()).executes((context) -> {
                    String username = StringArgumentType.getString(context, "username");
                    Text tomeGivenMessage = Text.literal("§b\uDAFF\uDFFC\uE006\uDAFF\uDFFF\uE002\uDAFF\uDFFE§b §epixlze rewarded §ea Guild Tome§b to §e" + username);
                    WynnChatMessage.EVENT.invoker().interact(tomeGivenMessage);
                    McUtils.sendLocalMessage(tomeGivenMessage, Text.empty(), false);
                    return Command.SINGLE_SUCCESS;
                })));
            });
        }
    }

    private void onWynnMessage(Text message) {
        if (!MinecraftClient.getInstance().isOnThread()) {
            GuildApi.LOGGER.info("not render thread message");
            return;
        }
        String tomeMessage = TextUtils.parseStyled(message, TextParseOptions.DEFAULT.withExtractUsernames(true));
        Matcher tomeMatcher = TOME_MESSAGE_PATTERN.matcher(tomeMessage);
        if (tomeMatcher.find()) {
            deleteTome(tomeMatcher.group("giver"), tomeMatcher.group("receiver"));
        }
    }

    private void deleteTome(String giver, String receiver) {
        GuildApi.LOGGER.info("{} gave a tome to {}", giver, receiver);
//        Managers.Net.guild.delete("guilds/tomes/" + Managers.Net.guild.guildId + "/" + receiver);
    }

    private void search(String username) {
        Managers.Net.guild.get(ENDPOINT + Managers.Net.guild.guildId + "/" + username).whenCompleteAsync((res, exception) -> {
            try {
                NetUtils.applyDefaultCallback(res, exception, (response) -> {
                    JsonObject resBody = response.getAsJsonObject();
                    McUtils.sendLocalMessage(Text.literal(resBody.get("username").getAsString() + " is at position " + resBody.get("position").getAsString() + " in the tome queue.").withColor(0xFFFFFF), Prepend.DEFAULT.get(), false);
                }, (error) -> {
                    if (error.equals("Specified user could not be found in tome list."))
                        McUtils.sendLocalMessage(Text.literal("§eCould not find \"" + username + "\" in the tome queue."), Prepend.DEFAULT.get(), false);
                    else
                        McUtils.sendLocalMessage(Text.literal("§cCould not fetch tome list. Reason: " + error), Prepend.DEFAULT.get(), false);
                });
            } catch (Exception e) {
                NetUtils.defaultException("tomelist search", e);
            }
        });
    }
}
