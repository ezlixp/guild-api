package pixlze.mod.features.chat_regex;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import oshi.util.tuples.Pair;
import pixlze.mod.config.PixUtilsConfig;
import pixlze.mod.config.types.SubConfig;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class ChatNotifications {
    static public SubConfig<Pair<Pattern, String>> config;
    private static int messageTimer = 0;
    private static boolean showMessage = false;
    private static Text message = Text.of("placeholder");

    public static void initialize() {
        HudRenderCallback.EVENT.register((context, delta) -> {
            if (showMessage) {
//                context.getMatrices().scale(2, 2, 1);
                context.drawText(MinecraftClient.getInstance().textRenderer, message, MinecraftClient.getInstance().getWindow().getScaledWidth() / 2 - (MinecraftClient.getInstance().textRenderer.getWidth(message) / 2), MinecraftClient.getInstance().getWindow().getScaledHeight() / 2 + 20, 0xFF0000, false);
//                context.getMatrices().scale(0.5F, 0.5F, 1);

            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (messageTimer > 0)
                --messageTimer;
            else
                showMessage = false;

        });
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            for (Pair<Pattern, String> c : config.getValue()) {
                if (c.getA().matcher(message.getString()).matches()) {
                    System.out.println(c.getB());
                    ChatNotifications.message = Text.of(c.getB());
                    messageTimer = 40;
                    showMessage = true;
                }
            }
        });
        EditNotificationsScreen editNotificationsScreen = new EditNotificationsScreen();
        config = PixUtilsConfig.registerSubConfig("Notifications", "Edit Notifications", editNotificationsScreen, new ArrayList<Pair<Pattern, String>>());
    }
}
