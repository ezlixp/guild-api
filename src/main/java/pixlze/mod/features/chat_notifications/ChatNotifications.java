package pixlze.mod.features.chat_notifications;

import com.google.gson.JsonElement;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import pixlze.mod.PixUtils;
import pixlze.mod.config.PixUtilsConfig;
import pixlze.mod.config.types.SubConfig;
import pixlze.utils.ApiInfo;
import pixlze.utils.Visitors;
import pixlze.utils.requests.CompletedRaidPojo;

import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ChatNotifications {
    static final String FEATURE_NAME = "Chat notifications";
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
            Visitors.currentVisit = new StringBuilder();
            Optional<String> visited = message1.visit(Visitors.STYLED_VISITOR, message1.getStyle());
            if (visited.isPresent()) return;
            for (Pair<Pattern, String> c : config.getValue()) {
                if (c.getLeft().matcher(Visitors.currentVisit).matches()) {
                    ChatNotifications.message = Text.of(c.getRight());
                    messageTimer = 40;
                    showMessage = true;
                }
            }
        }));
        ClientReceiveMessageEvents.GAME.register(((message1, overlay) -> {
            if (overlay) return;
            Visitors.currentVisit = new StringBuilder();
            Optional<String> visited = message1.visit(Visitors.STYLED_VISITOR, message1.getStyle());
            if (visited.isPresent()) return;
            for (Pair<Pattern, String> c : config.getValue()) {
                if (c.getLeft().matcher(Visitors.currentVisit).find()) {
                    ChatNotifications.message = Text.of(c.getRight());
                    messageTimer = 40;
                    showMessage = true;
                }
            }
            Visitors.currentVisit = new StringBuilder();
            message1.visit(Visitors.RAID_VISITOR, message1.getStyle());
            Matcher raidMatcher = Pattern.compile(".*&e(.*?)&b.*&e(.*?)&b.*&e(.*?)&b.*&e(.*?)&b.*?&3(.*?)&b").matcher(Visitors.currentVisit);
            if (raidMatcher.find() && !Visitors.currentVisit.toString().contains(":")) {
                ChatNotifications.message = Text.of("guild raid finished");
                messageTimer = 40;
                showMessage = true;
                new Thread(() -> {
                    try {
                        HttpPost post = new HttpPost(PixUtils.secrets.get("guild_raid_urls").getAsJsonObject().get(ApiInfo.wynnPlayerInfo.get("guild").getAsJsonObject().get("prefix").getAsString()).getAsString() + "addRaid");
                        StringEntity body = new StringEntity(PixUtils.gson.toJson(new CompletedRaidPojo(new String[]{raidMatcher.group(1), raidMatcher.group(2), raidMatcher.group(3), raidMatcher.group(4)}, raidMatcher.group(5), System.currentTimeMillis())));
                        post.setEntity(body);
                        post.setHeader("Content-type", "application/json");
                        post.setHeader("Authorization", "Bearer " + ApiInfo.guildRaidServerToken);
                        HttpResponse response = PixUtils.httpClient.execute(post);
                        PixUtils.LOGGER.info("{} guild raid response", EntityUtils.toString(response.getEntity()));
                    } catch (Exception e) {
                        PixUtils.LOGGER.error("guild raid post error: {} {}", e, e.getMessage());
                    }
                }).start();
            }
        }));

        ClientReceiveMessageEvents.GAME_CANCELED.register((message, overlay) -> {
            PixUtils.LOGGER.info("{} {} game cancelled", message, overlay);
        });
        ArrayList<Pair<Pattern, String>> prev = new ArrayList<>();
        if (PixUtilsConfig.configObject != null) {
            for (JsonElement item : PixUtilsConfig.configObject.get(FEATURE_ID).getAsJsonArray()) {
                try {
                    prev.add(new Pair<>(Pattern.compile(item.getAsJsonObject().get("left").getAsString()), item.getAsJsonObject().get("right").getAsString()));
                } catch (Exception e) {
                    PixUtils.LOGGER.error(e.getMessage());
                }
            }
        }
        EditNotificationsScreen editNotificationsScreen = new EditNotificationsScreen();
        config = PixUtilsConfig.registerSubConfig(FEATURE_NAME, FEATURE_ID, "Edit", editNotificationsScreen, prev);
    }
}
