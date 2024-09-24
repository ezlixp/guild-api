package pixlze.guildapi.features.list;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.features.Feature;
import pixlze.guildapi.net.GuildApiClient;
import pixlze.guildapi.utils.McUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class ListFeature extends Feature {
    private final String name;
    private final String endpoint;
    private final Function<JsonObject, MutableText> lineParser;
    private JsonElement cachedResponse;

    public ListFeature(String name, String endpoint, Function<JsonObject, MutableText> lineParser) {
        this.name = name;
        this.endpoint = endpoint;
        this.lineParser = lineParser;
    }

    @Override
    public void init() {
        registerCommands(List.of());
    }

    public void registerCommands(List<LiteralArgumentBuilder<FabricClientCommandSource>> subCommands) {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
                    LiteralArgumentBuilder<FabricClientCommandSource> builder = ClientCommandManager.literal(name + "list")
                            .executes(context -> {
                                listItems(0, true);
                                return 0;
                            }).then(ClientCommandManager.literal("view")
                                    .then(ClientCommandManager.argument("page", IntegerArgumentType.integer(1))
                                            .executes(context -> {
                                                int page = IntegerArgumentType.getInteger(context, "page");
                                                listItems(page - 1, true);
                                                return 0;
                                            }).then(ClientCommandManager.argument("reload", BoolArgumentType.bool())
                                                    .executes(context -> {
                                                        int page = IntegerArgumentType.getInteger(context, "page");
                                                        boolean reload = BoolArgumentType.getBool(context, "reload");
                                                        listItems(page - 1, reload);
                                                        return 0;
                                                    })
                                            )));
                    for (LiteralArgumentBuilder<FabricClientCommandSource> subCommand : subCommands) {
                        builder.then(subCommand);
                    }
                    dispatcher.register(builder);
                }
        );
    }

    private void listItems(int page, boolean reload) {
        CompletableFuture<JsonElement> response = new CompletableFuture<>();
        if (reload) response = Managers.Net.getApi("guild", GuildApiClient.class).get(endpoint);
        else response.complete(cachedResponse);
        response.whenCompleteAsync((res, exception) -> {
            cachedResponse = res;
            if (res == null) {
                assert Formatting.YELLOW.getColorValue() != null;
                if (!reload) McUtils.sendLocalMessage(Text.literal("No cached list data")
                        .withColor(Formatting.YELLOW.getColorValue()));
                return;
            }
            List<JsonElement> listItems = res.getAsJsonArray().asList();
            MutableText listMessage = Text.literal(name.substring(0, 1)
                            .toUpperCase() + name.substring(1) + " list page " + (page + 1) + ":\n")
                    .setStyle(Style.EMPTY.withColor(Formatting.WHITE));
            for (int i = 5 * page; i < 5 * (page + 1); i++) {
                if (i >= listItems.size())
                    break;
                listMessage.append(Text.literal(i + 1 + ". ")).withColor(0xFFFFFF);
                listMessage.append(lineParser.apply(listItems.get(i).getAsJsonObject()));
                if (i != Math.min(page, listItems.size()) - 1) {
                    listMessage.append(Text.literal("\n"));
                }
            }
            boolean hasPrev = page > 0;
            boolean hasNext = 5 * (page + 1) < listItems.size();
            listMessage.append("\n");
            listMessage.append(Text.literal("<< Prev")
                            .setStyle(Style.EMPTY.withColor(hasPrev ? Formatting.GREEN:Formatting.GRAY).withBold(true)
                                    .withClickEvent(hasPrev ? new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + name + "list view " + page + " false"):null)))
                    .append("          ").append(Text.literal("Next >>")
                            .setStyle(Style.EMPTY.withColor(hasNext ? Formatting.GREEN:Formatting.GRAY).withBold(true)
                                    .withClickEvent(hasNext ? new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + name + "list view " + (page + 2) + " false"):null)));
            listMessage.append("\n");
            McUtils.sendLocalMessage(listMessage);
        });
    }
}
