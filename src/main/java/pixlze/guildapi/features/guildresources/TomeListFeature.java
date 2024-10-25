package pixlze.guildapi.features.guildresources;

import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.features.type.ListFeature;
import pixlze.guildapi.handlers.chat.event.ChatMessageReceived;
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
    private final Pattern TOME_MESSAGE_PATTERN = Pattern.compile("^§.((\uDAFF\uDFFC\uE001\uDB00\uDC06)|(\uDAFF\uDFFC\uE006\uDAFF\uDFFF\uE002\uDAFF\uDFFE))§. §.(?<giver>.*?)(§.)?" + " rewarded §.a Guild Tome§. to §.(?<receiver>.*?)(§.)?$");

    public TomeListFeature() {
        super("tome", "tomes", (listItem) -> Text.literal(listItem.getAsJsonObject().get("username").getAsString()).setStyle(Style.EMPTY.withColor(Formatting.WHITE)));
    }

    @Override
    public void init() {
        ChatMessageReceived.EVENT.register(this::onWynnMessage);
        super.registerCommands(List.of(ClientCommandManager.literal("add").executes((context) -> {
            Managers.Net.guild.post("tomes", JsonUtils.toJsonObject("{\"username\":\"" + McUtils.playerName() + "\"}")).whenCompleteAsync((res, exception) -> {
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
    }

    private void onWynnMessage(Text message) {
        if (!MinecraftClient.getInstance().isOnThread()) {
            GuildApi.LOGGER.info("not render thread message");
            return;
        }
        String tomeMessage = TextUtils.parseStyled(message, TextParseOptions.DEFAULT.withExtractUsernames(true));
        Matcher tomeMatcher = TOME_MESSAGE_PATTERN.matcher(tomeMessage);
        if (tomeMatcher.find()) {
            GuildApi.LOGGER.info("{} gave a tome to {}", tomeMatcher.group("giver"), tomeMatcher.group("receiver"));
            Managers.Net.guild.delete("tomes/" + tomeMatcher.group("receiver"));
        }
    }

    private void search(String username) {
        Managers.Net.guild.get("tomes/" + username).whenCompleteAsync((res, exception) -> {
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
