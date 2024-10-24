package pixlze.guildapi.features.guildresources;

import com.google.gson.JsonElement;
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
import pixlze.guildapi.utils.text.TextUtils;
import pixlze.guildapi.utils.text.type.TextParseOptions;
import pixlze.guildapi.utils.type.Prepend;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TomeListFeature extends ListFeature {
    private final Pattern TOME_MESSAGE_PATTERN = Pattern.compile("^§.((\uDAFF\uDFFC\uE001\uDB00\uDC06)|(\uDAFF\uDFFC\uE006\uDAFF\uDFFF\uE002\uDAFF\uDFFE))§. §.(?<giver>.*?)(§.)?" +
            " rewarded §.a Guild Tome§. to §.(?<receiver>.*?)(§.)?$");

    public TomeListFeature() {
        super("tome", "tomes", (listItem) ->
                Text.literal(listItem.getAsJsonObject().get("username")
                        .getAsString()).setStyle(Style.EMPTY.withColor(Formatting.WHITE)));
    }

    @Override
    public void init() {
        ChatMessageReceived.EVENT.register(this::onWynnMessage);
        super.registerCommands(
                List.of(ClientCommandManager.literal("add").executes((context) -> {
                                    Managers.Net.guild.post("tomes", JsonUtils.toJsonObject("{\"username\":\"" + McUtils.playerName() + "\"}"), true);
                                    return Command.SINGLE_SUCCESS;

                                }
                        ), ClientCommandManager.literal("search").executes((context) -> {
                                    search(McUtils.playerName());
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(ClientCommandManager.argument("username", StringArgumentType.word())
                                        .executes((context) -> {
                                            search(StringArgumentType.getString(context, "username"));
                                            return Command.SINGLE_SUCCESS;
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
        String tomeMessage = TextUtils.parseStyled(message, TextParseOptions.DEFAULT.withExtractUsernames(true));
        Matcher tomeMatcher = TOME_MESSAGE_PATTERN.matcher(tomeMessage);
        if (tomeMatcher.find()) {
            GuildApi.LOGGER.info("{} gave a tome to {}", tomeMatcher.group("giver"), tomeMatcher.group("receiver"));
            Managers.Net.guild.delete("tomes/" + tomeMatcher.group("receiver"), false);
        }
    }

    private void search(String username) {
        CompletableFuture<JsonElement> response = Managers.Net.guild.get("tomes/" + username);
        response.whenCompleteAsync((res, exception) -> {
            if (exception == null && res != null) {
                McUtils.sendLocalMessage(Text.literal(res.getAsJsonObject()
                        .get("username")
                        .getAsString() + " is at position " + res
                        .getAsJsonObject()
                        .get("position").getAsString() + ".").withColor(0xFFFFFF), Prepend.DEFAULT.get(), false);
            }
        });
    }
}
