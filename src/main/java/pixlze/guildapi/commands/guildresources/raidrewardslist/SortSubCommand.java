package pixlze.guildapi.commands.guildresources.raidrewardslist;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import pixlze.guildapi.core.commands.ClientCommand;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.type.Prepend;

import java.util.function.Consumer;

class SortSubCommand extends ClientCommand {
    private final Consumer<String> setSortMember;

    public SortSubCommand(Consumer<String> setSortMember) {
        super("sort");
        this.setSortMember = setSortMember;
    }

    @Override
    protected LiteralArgumentBuilder<FabricClientCommandSource> getCommand(LiteralArgumentBuilder<FabricClientCommandSource> base) {
        return ClientCommandManager.literal("sort")
                .then(ClientCommandManager.literal("raids").executes(context -> {
                    setSortMember.accept("raids");
                    McUtils.sendLocalMessage(Text.literal("Successfully set raid list to sort by \"raids\"."), Prepend.DEFAULT.get(), false);
                    return Command.SINGLE_SUCCESS;
                })).then(ClientCommandManager.literal("aspects").executes(context -> {
                    setSortMember.accept("aspects");
                    McUtils.sendLocalMessage(Text.literal("Successfully set raid list to sort by \"aspects\"."), Prepend.DEFAULT.get(), false);
                    return Command.SINGLE_SUCCESS;
                })).then(ClientCommandManager.literal("emeralds").executes(context -> {
                    setSortMember.accept("liquidEmeralds");
                    McUtils.sendLocalMessage(Text.literal("Successfully set raid list to sort by \"emeralds\"."), Prepend.DEFAULT.get(), false);
                    return Command.SINGLE_SUCCESS;
                })).executes(context -> {
                    syntaxError();
                    return Command.SINGLE_SUCCESS;
                });
    }
}
