package pixlze.guildapi.commands.guildresources.raidrewardslist;

import com.google.gson.JsonElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.commands.type.ListClientCommand;
import pixlze.guildapi.core.commands.ClientCommand;
import pixlze.guildapi.core.handlers.chat.event.ChatMessageReceived;
import pixlze.guildapi.utils.text.TextUtils;
import pixlze.guildapi.utils.text.type.TextParseOptions;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RaidRewardsListClientCommand extends ListClientCommand {
    private static final Pattern ASPECT_MESSAGE_PATTERN = Pattern.compile("^§.((\uDAFF\uDFFC\uE001\uDB00\uDC06)|(\uDAFF\uDFFC\uE006\uDAFF\uDFFF\uE002\uDAFF\uDFFE))§. §.(?<giver>.*?)(§" +
            ".)? rewarded §.an Aspect§. to §.(?<receiver>.*?)(§.)?$");
    private static final String ENDPOINT = "guilds/raids/rewards/";

    public RaidRewardsListClientCommand() {
        super("raid", ENDPOINT, RaidRewardsListClientCommand::formatLine, "raids");
        ChatMessageReceived.EVENT.register(this::onWynnMessage);
    }

    @Override
    protected List<String> getAliases() {
        return List.of("rl");
    }

    @Override
    protected List<ClientCommand> getSubCommands() {
        return List.of(new SearchSubCommand(), new SortSubCommand(super::setSortMember));
    }

    private static MutableText formatLine(JsonElement listItem, String sortMember) {
        List<Pair<MutableText, String>> components = new java.util.ArrayList<>(List.of(
                new Pair<>(Text.literal(listItem.getAsJsonObject().get("raids").getAsString()).append(" raids"), "raids"),
                new Pair<>(Text.literal(listItem.getAsJsonObject().get("aspects").getAsString()).append(" aspects"), "aspects"),
                new Pair<>(Text.literal(String.format("%.2f", listItem.getAsJsonObject().get("liquidEmeralds").getAsDouble())).append(" ¼²"), "liquidEmeralds")
        ));
        components.sort((a, b) -> {
            if (a.getRight().equals(sortMember)) return -1;
            if (b.getRight().equals(sortMember)) return 1;
            return 0;
        });
        MutableText out = Text.literal(listItem.getAsJsonObject().get("username")
                .getAsString()).append(": ");
        for (int i = 0; i < components.size() - 1; i++) {
            out.append(components.get(i).getLeft()).append(" | ");
        }
        out.append(components.getLast().getLeft());
        return out;
    }

    private void onWynnMessage(Text message) {
        if (!MinecraftClient.getInstance().isOnThread()) {
            GuildApi.LOGGER.info("not render thread message");
            return;
        }
        String aspectMessage = TextUtils.parseStyled(message, TextParseOptions.DEFAULT.withExtractUsernames(true));
        Matcher aspectMatcher = ASPECT_MESSAGE_PATTERN.matcher(aspectMessage);
        if (aspectMatcher.find()) {
            GuildApi.LOGGER.info("{} gave an aspect to {}", aspectMatcher.group("giver"), aspectMatcher.group("receiver"));
        }
    }

}
