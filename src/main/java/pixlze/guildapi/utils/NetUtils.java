package pixlze.guildapi.utils;

import com.google.gson.JsonElement;
import net.minecraft.text.Text;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.core.ErrorMessages;
import pixlze.guildapi.core.components.Managers;
import pixlze.guildapi.utils.type.Prepend;

import java.net.http.HttpResponse;
import java.util.function.Consumer;

public class NetUtils {
    /**
     * @param response  http response object from api request
     * @param exception exception from api request
     * @param onSuccess consumer for successful api calls (2xx)
     * @param onFailed  consumer for unsuccessful api calls
     * @throws Exception throws an exception on fatal api errors
     */
    public static void applyDefaultCallback(HttpResponse<String> response, Throwable exception, Consumer<JsonElement> onSuccess, Consumer<String> onFailed) throws Exception {
        if (exception != null && exception.getMessage().equals(ErrorMessages.API_DISABLED_ERROR)) return;
        if (exception != null) {
            throw (Exception) exception;
        }
        if (response.statusCode() / 100 == 2) {
            onSuccess.accept(Managers.Json.toJsonElement(response.body()));
        } else {
            String error = Managers.Json.toJsonObject(response.body()).get("errorMessage").getAsString();
            if (error.equals(ErrorMessages.INVALID_TOKEN)) return;
            onFailed.accept(error);
        }
    }

    public static Consumer<String> defaultFailed(String name, boolean feedback) {
        return (error) -> {
            if (feedback)
                McUtils.sendLocalMessage(Text.literal("§c" + name + " failed. Reason: " + error), Prepend.DEFAULT.get(), false);
            GuildApi.LOGGER.error("{} error: {}", name, error);
        };
    }

    @Deprecated
    public static void defaultException(String name, Exception e) {
        McUtils.sendLocalMessage(Text.literal("§cSomething went wrong. Check logs for more details."), Prepend.DEFAULT.get(), false);
        GuildApi.LOGGER.error("{} exception: {} {}", name, e, e.getMessage());
    }
}
