package pixlze.guildapi.features;

import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.components.Feature;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.type.Prepend;

import java.util.List;

public class TestCommandHelpFeature extends Feature {
    private final List<Pair<String, String>> commands = List.of(
            new Pair<>("/setplayer <username>", "Impersonates specified username."),
            new Pair<>("/raid <notg|nol|tcc|tna> <player1> <player2> <player3> <player 4>", "Simulates raid completion."),
            new Pair<>("/tome <username>", "Simulates a tome given to specified username."),
            new Pair<>("/aspect <username>", "Simulates an aspect given to specified username."),
            new Pair<>("/testmessage <message>", "Simulates a guild message.")
    );

    private MutableText helpMessage;

    @Override
    public void init() {
        helpMessage = Text.literal("§aTest Commands:\n");
        for (int i = 0; i < commands.size(); i++) {
            Pair<String, String> entry = commands.get(i);
            String delimiter = entry.getLeft().isBlank() ? "":" - ";
            helpMessage.append(entry.getLeft() + delimiter + entry.getRight());
            if (i != commands.size() - 1)
                helpMessage.append("\n");
        }
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> {
            dispatcher.register(GuildApi.BASE_COMMAND.then(ClientCommandManager.literal("testhelp").executes((context) -> {
                McUtils.sendLocalMessage(helpMessage, Prepend.DEFAULT.get(), false);
                return Command.SINGLE_SUCCESS;
            })));
        }));
    }
}
