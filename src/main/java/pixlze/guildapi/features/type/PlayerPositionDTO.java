package pixlze.guildapi.features.type;

import net.minecraft.util.math.Vec3d;

public class PlayerPositionDTO {
    public double x, y, z;

    public PlayerPositionDTO(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static PlayerPositionDTO fromVec3d(Vec3d pos) {
        return new PlayerPositionDTO(pos.x, pos.y, pos.z);
    }
}
