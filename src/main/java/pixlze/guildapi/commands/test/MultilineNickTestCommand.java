package pixlze.guildapi.commands.test;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import pixlze.guildapi.core.commands.ClientCommand;
import pixlze.guildapi.mc.event.WynnChatMessage;
import pixlze.guildapi.utils.ColourUtils;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.text.FontUtils;
import pixlze.guildapi.utils.text.TextUtils;

public class MultilineNickTestCommand extends ClientCommand {
    private static final Text MULTILINE_NICK = Text.empty().setStyle(ColourUtils.AQUA)
            .append(Text.literal("\uDAFF\uDFFC\uE001\uDB00\uDC06")
                    .setStyle(Style.EMPTY.withFont(FontUtils.PrefixFont.get())))
            .append(" ")
            .append(Text.empty().setStyle(ColourUtils.DARK_AQUA)
                    .append(Text.literal("w w w w w w w w w w")
                            .setStyle(TextUtils.realNameStyle("w w w w w w w w w w", "dawnlia")))
                    .append(" rewarded ")
                    .append(Text.literal("1024 Emeralds").setStyle(ColourUtils.YELLOW))
                    .append(" to ")
                    .append(Text.empty().setStyle(ColourUtils.DARK_AQUA)
                            .append(Text.literal("w w w w w")
                                    .setStyle(TextUtils.realNameStyle("w w w w w w w w w w", "dawnlia"))
                                    .append(Text.literal("\n").append(Text.empty()
                                            .setStyle(ColourUtils.AQUA.withBold(false)
                                                    .withUnderline(false)).append(
                                                    Text.literal("\uDAFF\uDFFC\uE001\uDB00\uDC06")
                                                            .setStyle(TextUtils.fontOf(Identifier.of("chat/prefix")))
                                            ).append(" "))).append("w w w w w"))
                    )
            );

    public MultilineNickTestCommand() {
        super("multiline");
    }

    @Override
    protected LiteralArgumentBuilder<FabricClientCommandSource> getCommand(LiteralArgumentBuilder<FabricClientCommandSource> base) {
        return base.executes((context) -> {
            WynnChatMessage.EVENT.invoker().interact(MULTILINE_NICK);
            McUtils.sendLocalMessage(MULTILINE_NICK, Text.empty(), false);
            return Command.SINGLE_SUCCESS;
        });
    }
}
