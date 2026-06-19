package pixlze.guildapi.core.waypoints;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import pixlze.guildapi.utils.McUtils;
import pixlze.guildapi.utils.text.TextUtils;

public class Waypoint {
    private DisplayEntity.TextDisplayEntity name;
    private DisplayEntity.TextDisplayEntity distance;
    private int curdist;
    // active is if we are choosing to show it, enabled is if we are allowed to show it
    private boolean active;
    private boolean enabled;
    private boolean added;
    private final String username;
    private Vec3d realPos;

    public Waypoint(String username, double x, double y, double z) {
        this.username = username;
        this.realPos = new Vec3d(x, y, z);
        this.createEntities();
        active = false;
        enabled = false;
        added = false;
    }

    private void createEntities() {
        name = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, McUtils.mc().world);
        distance = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, McUtils.mc().world);
        name.setText(Text.empty().append(Text.literal("\uDAFF\uDFF8\uE014\uDAFF\uDFDE\uE008")
                        .setStyle(TextUtils.fontOf(Identifier.of("marker")))).append(Text.literal("\n"))
                .append(Text.literal(username).setStyle(TextUtils.fontOf(Identifier.of("offset/five/-8")))));
        name.setBillboardMode(DisplayEntity.BillboardMode.CENTER);
        name.setDisplayFlags((byte) (name.getDisplayFlags() | 2));
        name.setBackground(0);
        distance.setBillboardMode(DisplayEntity.BillboardMode.CENTER);
        distance.setDisplayFlags((byte) (distance.getDisplayFlags() | 2));
        distance.setBackground(0);
    }


    /**
     * Shows the waypoint if conditions are met.
     */
    public void tryShow() {
        if (!added && active && enabled) {
            this.update();
            assert McUtils.mc().world != null;
            McUtils.mc().world.addEntity(name);
            McUtils.mc().world.addEntity(distance);
            added = true;
        }
    }

    public void hide() {
        if (added && McUtils.mc().world != null) {
            McUtils.mc().world.removeEntity(name.getId(), Entity.RemovalReason.DISCARDED);
            McUtils.mc().world.removeEntity(distance.getId(), Entity.RemovalReason.DISCARDED);
            this.createEntities();
            added = false;
        }
    }

    public void update(double x, double y, double z) {
        if (McUtils.mc().player == null)
            return;
        this.realPos = new Vec3d(x, y, z);
        assert McUtils.mc().player != null;
        Vec3d playerPos = new Vec3d(McUtils.mc().player.getX(), McUtils.mc().player.getY(), McUtils.mc().player.getZ());
        if (playerPos.distanceTo(this.realPos) < 10) {
            name.setPosition(this.realPos.add(0, 2.4, 0));
            distance.setPosition(this.realPos.add(0, 2.2, 0));
        } else {
            Vec3d dir = this.realPos.subtract(playerPos).normalize().multiply(7);
            name.setPosition(dir.add(playerPos).add(0, 2.4, 0));
            distance.setPosition(dir.add(playerPos).add(0, 2.2, 0));
        }
        distance.setText(Text.literal(Math.round(playerPos.distanceTo(this.realPos)) + "m")
                .setStyle(TextUtils.fontOf(Identifier.of("offset/five/-8"))));
    }

    public void update() {
        this.update(this.realPos.getX(), this.realPos.getY(), this.realPos.getZ());
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled)
            tryShow();
        else hide();
    }

    public void setActive(boolean active) {
        this.active = active;
        if (active)
            tryShow();
        else hide();
    }

    public boolean isActive() {
        return active;
    }

    public String getUsername() {
        return username;
    }
}
