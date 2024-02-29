package scripts.factions.data.obj

import groovy.transform.CompileStatic
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World

@CompileStatic
class Position {
    public String world

    public double x1, y1, z1
    public float yaw = 0F, pitch = 0F

    Position() {}

    Position(double x1, double y1, double z1) {
        this.x1 = x1
        this.y1 = y1
        this.z1 = z1
    }

    Position(double x1, double y1, double z1, float yaw, float pitch) {
        this.x1 = x1
        this.y1 = y1
        this.z1 = z1
        this.yaw = yaw
        this.pitch = pitch
    }

    Position(String world, double x1, double y1, double z1) {
        this.world = world
        this.x1 = x1
        this.y1 = y1
        this.z1 = z1
    }

    Position(String world, double x1, double y1, double z1, float yaw, float pitch) {
        this.world = world
        this.x1 = x1
        this.y1 = y1
        this.z1 = z1
        this.yaw = yaw
        this.pitch = pitch
    }

    @BsonIgnore
    Location getLocation(World givenWorld) {
        if (givenWorld != null) {
            return new Location(givenWorld, x1, y1, z1, yaw, pitch)
        } else {
            return new Location(Bukkit.getWorld(world), x1, y1, z1, yaw, pitch)
        }
    }

    @Override
    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof Position)) return false

        Position position = (Position) o
        if (Double.compare(position.x1, x1) != 0) return false
        if (Double.compare(position.y1, y1) != 0) return false
        if (Double.compare(position.z1, z1) != 0) return false
        if (Double.compare(position.yaw, yaw) != 0) return false
        if (Double.compare(position.pitch, pitch) != 0) return false
        if (world != null ? world != position.world : position.world != null) return false

        return true
    }
    @BsonIgnore
    static def of(Location location) {
        return new Position(location.world.name, location.blockX, location.blockY, location.blockZ, location.yaw, location.pitch)
    }

    @Override
    String toString() {
        return "Position{" +
                "world='" + world + '\'' +
                ", x=" + x1 +
                ", y=" + y1 +
                ", z=" + z1 +
                '}'
    }

}
