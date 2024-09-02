package pixlze.pixutils.features.chat_notifications;

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
import pixlze.pixutils.PixUtils;
import pixlze.pixutils.components.Managers;
import pixlze.pixutils.config.types.SubConfig;
import pixlze.pixutils.net.ApiManager;
import pixlze.pixutils.net.models.CompletedRaidModel;
import pixlze.pixutils.utils.ChatUtils;

import java.util.ArrayList;
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
        ClientReceiveMessageEvents.GAME.register((message1, overlay) -> {
            if (overlay) return;
            String styledMessage = ChatUtils.parseStyled(message1);
            for (Pair<Pattern, String> c : config.getValue()) {
                if (c.getLeft().matcher(styledMessage).find()) {
                    ChatNotifications.message = Text.of(c.getRight());
                    messageTimer = 40;
                    showMessage = true;
                }
            }
            String raidMessage = ChatUtils.parseRaid(message1);
            Matcher raidMatcher = Pattern.compile(".*§e(.*?)§b.*§e(.*?)§b.*§e(.*?)§b.*§e(.*?)§b.*?§3(.*?)§b").matcher(raidMessage);
            if (raidMatcher.find() && !raidMessage.contains(":")) {
                ChatNotifications.message = Text.of("guild raid finished");
                messageTimer = 40;
                showMessage = true;
                new Thread(() -> {
                    try {
                        HttpPost post = new HttpPost(PixUtils.secrets.get("guild_raid_urls").getAsJsonObject().get(ApiManager.wynnPlayerInfo.get("guild").getAsJsonObject().get("prefix").getAsString()).getAsString() + "addRaid");
                        StringEntity body = new StringEntity(PixUtils.gson.toJson(new CompletedRaidModel(new String[]{raidMatcher.group(1), raidMatcher.group(2), raidMatcher.group(3), raidMatcher.group(4)}, raidMatcher.group(5), System.currentTimeMillis())));
                        post.setEntity(body);
                        post.setHeader("Content-type", "application/json");
                        post.setHeader("Authorization", "Bearer " + ApiManager.guildRaidServerToken);
                        HttpResponse response = ApiManager.httpClient.execute(post);
                        PixUtils.LOGGER.info("{} guild raid response", EntityUtils.toString(response.getEntity()));
                    } catch (Exception e) {
                        PixUtils.LOGGER.error("guild raid post error: {} {}", e, e.getMessage());
                    }
                }).start();
            }
        });

        ArrayList<Pair<Pattern, String>> prev = new ArrayList<>();
        if (Managers.Config.configObject != null) {
            for (JsonElement item : Managers.Config.configObject.get(FEATURE_ID).getAsJsonArray()) {
                try {
                    prev.add(new Pair<>(Pattern.compile(item.getAsJsonObject().get("left").getAsString()), item.getAsJsonObject().get("right").getAsString()));
                } catch (Exception e) {
                    PixUtils.LOGGER.error(e.getMessage());
                }
            }
        }
        EditNotificationsScreen editNotificationsScreen = new EditNotificationsScreen();
        config = Managers.Config.registerSubConfig(FEATURE_NAME, FEATURE_ID, "Edit", editNotificationsScreen, prev);
    }
}
