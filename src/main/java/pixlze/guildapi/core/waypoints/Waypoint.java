package pixlze.guildapi.core.waypoints;

import com.mojang.authlib.GameProfile;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.text.TextUtils;

import java.util.UUID;

public class Waypoint {
    private final DisplayEntity.ItemDisplayEntity headDisplay = new DisplayEntity.ItemDisplayEntity(EntityType.ITEM_DISPLAY, McUtils.mc().world);
    private final DisplayEntity.TextDisplayEntity label = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, McUtils.mc().world);
    private int curdist;

    public Waypoint(String uuid, String username, int x, int y, int z) {
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        GameProfile profile = new GameProfile(UUID.fromString(uuid), username);
        ProfileComponent component = ProfileComponent.ofStatic(profile);
        head.set(DataComponentTypes.PROFILE, component);
        headDisplay.setItemStack(head);
        headDisplay.setPosition(x, y, z);
        label.setText(Text.literal("hisdjfaosiefjaoisejfioasjef").setStyle(TextUtils.fontOf(Identifier.of("default"))));
        label.setBillboardMode(DisplayEntity.BillboardMode.CENTER);
        label.setDisplayFlags((byte) (label.getDisplayFlags() | 2));
        label.setPosition(x, y, z);
        assert McUtils.mc().world != null;
        McUtils.mc().world.addEntity(headDisplay);
        McUtils.mc().world.addEntity(label);
    }
}
