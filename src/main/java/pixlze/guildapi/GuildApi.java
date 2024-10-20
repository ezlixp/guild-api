package pixlze.guildapi;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pixlze.guildapi.components.Handlers;
import pixlze.guildapi.components.Managers;
import pixlze.guildapi.components.Models;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.type.Prepend;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;


public class GuildApi implements ClientModInitializer {
    public static final String MOD_ID = "guildapi";
    public static final String MOD_STORAGE_ROOT = "guildapi";
    public static ModContainer MOD_CONTAINER;
    public static String MOD_VERSION;
    public static final Logger LOGGER = LoggerFactory.getLogger("guildapi");
    public static JsonObject secrets;
    public static LiteralArgumentBuilder<FabricClientCommandSource> BASE_COMMAND = ClientCommandManager.literal("guildapi").executes((context) -> {
        McUtils.sendLocalMessage(Text.of("§a§lGuild API §r§av" + MOD_VERSION + " by §lpixlze§r§a.\n§fType /guildapi help for a list of commands."), Prepend.DEFAULT.get(), false);
        return Command.SINGLE_SUCCESS;
    });
    private static boolean development;

    public static File getModStorageDir(String dirName) {
        return new File(MOD_STORAGE_ROOT, dirName);
    }

    public static boolean isDevelopment() {
        return development;
    }

    @Override
    public void onInitializeClient() {
        development = FabricLoader.getInstance().isDevelopmentEnvironment();
        if (FabricLoader.getInstance().getModContainer(MOD_ID).isPresent()) {
            MOD_CONTAINER = FabricLoader.getInstance().getModContainer(MOD_ID).get();
            MOD_VERSION = MOD_CONTAINER.getMetadata().getVersion().getFriendlyString();
        }

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            final LiteralCommandNode<FabricClientCommandSource> baseCommandNode = dispatcher.register(BASE_COMMAND);
            dispatcher.register(ClientCommandManager.literal("gapi").executes(baseCommandNode.getCommand()).redirect(baseCommandNode));
        });

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("secrets.json");
        if (inputStream == null) {
            GuildApi.LOGGER.error("secret variables could not be loaded");
        } else {
            secrets = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
        }


        Handlers.Chat.init();
        Managers.Connection.init();
        Managers.Net.init();
        Managers.Feature.init();
        Models.WorldState.init();
    }
}