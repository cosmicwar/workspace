package scripts.factions.content.entity.npc.navigable.path

import groovy.transform.CompileStatic
import org.bukkit.Location
import org.bukkit.World

@CompileStatic
class PathPoint {

    final double x, y, z
    float yaw, pitch

    PathPoint(double x, double y, double z, float yaw, float pitch) {
        this.x = x
        this.y = y
        this.z = z
        this.yaw = yaw
        this.pitch = pitch
    }

    Location toLocation(World world) {
        return new Location(world, x, y, z, yaw, pitch)
    }

    PathPoint offset(int x, int z) {
        return new PathPoint(this.x + x, y, this.z + z, yaw, pitch)
    }

    static PathPoint fromLocation(Location location) {
        return new PathPoint(location.x, location.y, location.z, location.yaw, location.pitch)
    }

}
