package scripts.factions.data.obj

import groovy.transform.CompileStatic
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World

@CompileStatic
class Position {
    public String world

    public double x, y, z
    public float yaw = 0F, pitch = 0F

    Position() {}

    Position(double x, double y, double z) {
        this.x = x
        this.y = y
        this.z = z
    }

    Position(double x, double y, double z, float yaw, float pitch) {
        this.x = x
        this.y = y
        this.z = z
        this.yaw = yaw
        this.pitch = pitch
    }

    Position(String world, double x, double y, double z) {
        this.world = world
        this.x = x
        this.y = y
        this.z = z
    }

    Position(String world, double x, double y, double z, float yaw, float pitch) {
        this.world = world
        this.x = x
        this.y = y
        this.z = z
        this.yaw = yaw
        this.pitch = pitch
    }

    @BsonIgnore
    Position toInt() {
        return new Position(world, (int) x, (int) y, (int) z, yaw, pitch)
    }

    @BsonIgnore
    Location getLocation(World givenWorld) {
        if (givenWorld != null) {
            return new Location(givenWorld, x, y, z, yaw, pitch)
        } else {
            return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch)
        }
    }

    @Override
    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof Position)) return false

        Position position = (Position) o
        if (Double.compare(position.x, x) != 0) return false
        if (Double.compare(position.y, y) != 0) return false
        if (Double.compare(position.z, z) != 0) return false
        if (Double.compare(position.yaw, yaw) != 0) return false
        if (Double.compare(position.pitch, pitch) != 0) return false
        if (world != null ? world != position.world : position.world != null) return false

        return true
    }
    @BsonIgnore
    static def of(Location location) {
        return new Position(location.world.name, location.getX(), location.getY(), location.getZ(), location.yaw, location.pitch)
    }

    @Override
    String toString() {
        return "Position{" +
                "world='" + world + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}'
    }

}
