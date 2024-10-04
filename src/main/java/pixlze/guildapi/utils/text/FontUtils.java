package pixlze.guildapi.utils.text;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import pixlze.guildapi.GuildApi;

public class FontUtils {
    public static class BannerPillFont {
        public static MutableText parseStringWithFill(String text) {
            StringBuilder finalText = new StringBuilder("\uE060\uDAFF\uDFFF");
            for (int i = 0; i < text.length(); i++) {
                int index = Character.toLowerCase(text.charAt(i)) - 'a';
                finalText.append(Character.toChars(index + 57392));
                finalText.append("\uDAFF\uDFFF");
            }
            finalText.append("\uE062");
            GuildApi.LOGGER.info("{}", finalText);
            return Text.literal(finalText.toString()).setStyle(Style.EMPTY.withFont(Identifier.of("banner/pill")));
        }
    }
}