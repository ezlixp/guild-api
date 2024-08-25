package pixlze.mod.features.chat_regex;

import com.google.gson.JsonElement;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import pixlze.mod.PixUtils;
import pixlze.mod.config.PixUtilsConfig;
import pixlze.mod.config.types.SubConfig;

import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Pattern;

import static pixlze.mod.PixUtils.currentVisit;

public class ChatNotifications {
    static final String FEATURE_ID = "chat_notifications";
    public static Text message = Text.of("placeholder");
    static public SubConfig<Pair<Pattern, String>> config;
    public static int messageTimer = 0;
    public static boolean showMessage = false;

    public static void initialize() {
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
        ClientReceiveMessageEvents.CHAT.register(((message1, signedMessage, sender, params, receptionTimestamp) -> {
            currentVisit = "";
            Optional<String> visited = message1.visit(PixUtils.wynnVisitor, message1.getStyle());
            if (visited.isPresent()) return;
            PixUtils.LOGGER.info("{} chat parsed {}", PixUtils.MOD_ID, currentVisit);
            if (message1.getString() == null) return;
            for (Pair<Pattern, String> c : config.getValue()) {
                if (c.getLeft().matcher(currentVisit).matches()) {
                    ChatNotifications.message = Text.of(c.getRight());
                    messageTimer = 40;
                    showMessage = true;
                }
            }
        }));
        ClientReceiveMessageEvents.GAME.register(((message1, overlay) -> {
            currentVisit = "";
            Optional<String> visited = message1.visit(PixUtils.wynnVisitor, message1.getStyle());
            if (visited.isPresent()) return;
            PixUtils.LOGGER.info("{} game parsed {}", PixUtils.MOD_ID, currentVisit);
            if (message1.getString() == null) return;
            for (Pair<Pattern, String> c : config.getValue()) {
                if (c.getLeft().matcher(currentVisit).matches()) {
                    ChatNotifications.message = Text.of(c.getRight());
                    messageTimer = 40;
                    showMessage = true;
                }
            }
            if (Pattern.matches(".*?&e(.*?)&b.*?&e(.*?).*?&e(.*?)&b.*?&e(.*?)&b.*?&3(.*?)&b.*", currentVisit)) {
                ChatNotifications.message = Text.of("guild raid finished");
                messageTimer = 40;
                showMessage = true;
            }
        }));
//
        ArrayList<Pair<Pattern, String>> prev = new ArrayList<>();
        if (PixUtilsConfig.configObject != null) {
            for (JsonElement item : PixUtilsConfig.configObject.get(FEATURE_ID).getAsJsonArray()) {
                prev.add(new Pair<>(Pattern.compile(item.getAsJsonObject().get("left").getAsString()), item.getAsJsonObject().get("right").getAsString()));
            }
        }
        EditNotificationsScreen editNotificationsScreen = new EditNotificationsScreen();
        config = PixUtilsConfig.registerSubConfig(FEATURE_ID, "Edit Notifications", editNotificationsScreen, prev);
    }
}
