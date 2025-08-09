package pixlze.guildapi.commands.guildresources.tomelist;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.commands.type.ListClientCommand;
import pixlze.guildapi.core.commands.ClientCommand;
import pixlze.guildapi.core.handlers.chat.event.ChatMessageReceived;
import pixlze.guildapi.utils.text.TextUtils;
import pixlze.guildapi.utils.text.type.TextParseOptions;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TomeListClientCommand extends ListClientCommand {
    private static final Pattern TOME_MESSAGE_PATTERN = Pattern.compile("^§.((\uDAFF\uDFFC\uE001\uDB00\uDC06)|(\uDAFF\uDFFC\uE006\uDAFF\uDFFF\uE002\uDAFF\uDFFE))§. §.(?<giver>.*?)(§.)? rewarded §.a Guild Tome§. to §.(?<receiver>.*?)(§.)?$");
    private static final String ENDPOINT = "guilds/tomes/";

    public TomeListClientCommand() {
        super("tome", ENDPOINT, (listItem) -> Text.literal(listItem.getAsJsonObject().get("username").getAsString()).setStyle(Style.EMPTY.withColor(Formatting.WHITE)));
        ChatMessageReceived.EVENT.register(this::onWynnMessage);
    }

    @Override
    protected List<String> getAliases() {
        return List.of("tl");
    }

    @Override
    protected List<ClientCommand> getSubCommands() {
        return List.of(new AddSubCommand(), new SearchSubCommand());
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

}
