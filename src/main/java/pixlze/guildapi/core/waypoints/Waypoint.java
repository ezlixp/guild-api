package pixlze.guildapi.core.waypoints;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import pixlze.guildapi.GuildApi;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.text.TextUtils;

public class Waypoint {
    private final DisplayEntity.TextDisplayEntity name = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, McUtils.mc().world);
    private final DisplayEntity.TextDisplayEntity distance = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, McUtils.mc().world);
    private int curdist;
    private boolean hidden;
    private Vec3d realPos;

    public Waypoint(String username, double x, double y, double z) {
        name.setText(Text.literal(username).setStyle(TextUtils.fontOf(Identifier.of("default"))));
        name.setBillboardMode(DisplayEntity.BillboardMode.CENTER);
        name.setDisplayFlags((byte) (name.getDisplayFlags() | 2));
        name.setBackground(0);
        distance.setBillboardMode(DisplayEntity.BillboardMode.CENTER);
        distance.setDisplayFlags((byte) (distance.getDisplayFlags() | 2));
        distance.setBackground(0);
        this.realPos = new Vec3d(x, y, z);
        hidden = true;
    }

    public void show() {
        if (!hidden || McUtils.mc().world == null) return;
        this.update();
        McUtils.mc().world.addEntity(name);
        McUtils.mc().world.addEntity(distance);
        hidden = false;
    }

    public void hide() {
        if (hidden || McUtils.mc().world == null) return;
        McUtils.mc().world.removeEntity(name.getId(), Entity.RemovalReason.DISCARDED);
        McUtils.mc().world.removeEntity(distance.getId(), Entity.RemovalReason.DISCARDED);
        hidden = true;
    }

    public void update(double x, double y, double z) {
        if (McUtils.mc().player == null) {
            GuildApi.LOGGER.warn("Tried to update waypoint but player is null.");
            return;
        }
        this.realPos = new Vec3d(x, y, z);
        assert McUtils.mc().player != null;
        Vec3d playerPos = new Vec3d(McUtils.mc().player.getX(), McUtils.mc().player.getY(), McUtils.mc().player.getZ());
        if (playerPos.distanceTo(this.realPos) < 100) {
            name.setPosition(this.realPos);
            distance.setPosition(this.realPos);
        } else {
            Vec3d dir = this.realPos.subtract(playerPos);
            dir.multiply(100); // show the waypoint 100 blocks away from the player
            name.setPosition(dir.add(playerPos));
            distance.setPosition(dir.add(playerPos));
        }
        distance.setText(Text.literal(Math.round(playerPos.distanceTo(this.realPos)) + "m"));
    }

    private void update() {
        this.update(this.realPos.getX(), this.realPos.getY(), this.realPos.getZ());
    }


}
