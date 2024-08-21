package pixlze.mod.features.chat_regex;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import oshi.util.tuples.Pair;
import pixlze.mod.PixUtils;
import pixlze.mod.config.PixUtilsConfig;
import pixlze.mod.config.types.SubConfig;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class ChatNotifications {
    public static Text message = Text.of("placeholder");
    static public SubConfig<Pair<Pattern, String>> config;
    public static int messageTimer = 0;
    public static boolean showMessage = false;

    public static void initialize() {
        PixUtils.LOGGER.info("init");
        HudRenderCallback.EVENT.register((context, delta) -> {
            if (showMessage) {
                context.drawText(MinecraftClient.getInstance().textRenderer, message, MinecraftClient.getInstance().getWindow().getScaledWidth() / 2 - (MinecraftClient.getInstance().textRenderer.getWidth(message) / 2), MinecraftClient.getInstance().getWindow().getScaledHeight() / 2 + 20, 0xFF0000, false);

            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (messageTimer > 0)
                --messageTimer;
            else
                showMessage = false;

        });
//        for (Pair<Pattern, String> c : config.getValue()) {
//                if (c.getA().matcher(message.getSignedContent()).matches()) {
//                    System.out.println(c.getB());
//                    ChatNotifications.message = Text.of(c.getB());
//                    messageTimer = 40;
//                    showMessage = true;
//                    PixUtils.LOGGER.info("chat notification captured");
//                }
//            }
//        ServerMessageEvents.CHAT_MESSAGE.register(((message1, sender, params) -> {
//            PixUtils.LOGGER.info("pixlistener1");
//        }));
//        ServerMessageEvents.GAME_MESSAGE.register(((message1, sender, params) -> {
//            PixUtils.LOGGER.info("pixlistener2");
//        }));
//        ServerMessageEvents.COMMAND_MESSAGE.register(((message1, sender, params) -> {
//            PixUtils.LOGGER.info("pixlistener3");
//        }));
//        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register(((message1, sender, params) -> {
//            PixUtils.LOGGER.info("pixlistener4");
//            return true;
//        }));
//        ServerMessageEvents.ALLOW_COMMAND_MESSAGE.register(((message1, sender, params) -> {
//            PixUtils.LOGGER.info("pixlistener5");
//            return true;
//        }));
//        ServerMessageEvents.ALLOW_GAME_MESSAGE.register(((message1, sender, params) -> {
//            PixUtils.LOGGER.info("pixlistener6");
//            return true;
//        }));
        ClientReceiveMessageEvents.CHAT.register(((message1, signedMessage, sender, params, receptionTimestamp) -> {
            if (message1.getContent().toString().isEmpty()) return;
            for (Pair<Pattern, String> c : config.getValue()) {
                if (c.getA().matcher(message.getContent().toString()).matches()) {
                    System.out.println(c.getB());
                    ChatNotifications.message = Text.of(c.getB());
                    messageTimer = 40;
                    showMessage = true;
                    PixUtils.LOGGER.info("chat notification captured from chat");
                }
            }
        }));
//        ClientReceiveMessageEvents.ALLOW_CHAT.register((a, b, c, d, e) -> {
//            PixUtils.LOGGER.info("pixlistener8");
//            return true;
//        });
        ClientReceiveMessageEvents.GAME.register(((message1, overlay) -> {
            if (message1.getContent().toString().isEmpty()) return;
            for (Pair<Pattern, String> c : config.getValue()) {
                if (c.getA().matcher(message.getContent().toString()).matches()) {
                    System.out.println(c.getB());
                    ChatNotifications.message = Text.of(c.getB());
                    messageTimer = 40;
                    showMessage = true;
                    PixUtils.LOGGER.info("chat notification captured from game");
                }
            }
        }));
//        ClientReceiveMessageEvents.ALLOW_GAME.register(((message1, overlay) -> {
//            PixUtils.LOGGER.info("pixlistener10");
//            return true;
//        }));
        EditNotificationsScreen editNotificationsScreen = new EditNotificationsScreen();
        config = PixUtilsConfig.registerSubConfig("Notifications", "Edit Notifications", editNotificationsScreen, new ArrayList<Pair<Pattern, String>>());
    }
}
