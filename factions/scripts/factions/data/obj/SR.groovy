package scripts.factions.data.obj

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block

@CompileStatic(TypeCheckingMode.SKIP)
class SR {

    public String world

    public int x1, y1, z1, x2, y2, z2

    SR() {}

    SR(String world, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.world = world
        if (x1 > x2) {
            this.x1 = x1
            this.x2 = x2
        } else {
            this.x2 = x1
            this.x1 = x2
        }
        if (y1 > y2) {
            this.y1 = y1
            this.y2 = y2
        } else {
            this.y2 = y1
            this.y1 = y2
        }
        if (z1 > z2) {
            this.z1 = z1
            this.z2 = z2
        } else {
            this.z2 = z1
            this.z1 = z2
        }
    }

    @BsonIgnore
    boolean contains(int x, int y, int z) {
        return (this.x2 <= x && x <= this.x1) && (this.y2 <= y && y <= this.y1) && (this.z2 <= z && z <= this.z1)
    }

    @BsonIgnore
    boolean contains(double x, double y, double z) {
        return (this.x2 <= x && x <= this.x1) && (this.y2 <= y && y <= this.y1) && (this.z2 <= z && z <= this.z1)
    }

    @BsonIgnore
    boolean contains(Location loc) {
        return contains(Math.floor(loc.getX()), Math.floor(loc.getY()), Math.floor(loc.getZ()))
    }

    @BsonIgnore
    boolean contains(Block block) {
        return contains(block.getLocation())
    }

    @BsonIgnore
    boolean containsHorizontal(int x, int z) {
        return (this.x2 <= x && x <= this.x1) && (this.z2 <= z && z <= this.z1)
    }

    @BsonIgnore
    boolean containsHorizontal(double x, double z) {
        return (this.x2 <= x && x <= this.x1) && (this.z2 <= z && z <= this.z1)
    }

    @BsonIgnore
    boolean overlaps(SR sr) {
        return (this.x1 >= sr.x2 && this.x2 <= sr.x1) && (this.y1 >= sr.y2 && this.y2 <= sr.y1) && (this.z1 >= sr.z2 && this.z2 <= sr.z1)
    }

    @BsonIgnore
    void reorder() {
        int x1 = this.x1
        int y1 = this.y1
        int z1 = this.z1
        int x2 = this.x2
        int y2 = this.y2
        int z2 = this.z2

        if (x1 > x2) {
            this.x1 = x1
            this.x2 = x2
        } else {
            this.x2 = x1
            this.x1 = x2
        }
        if (y1 > y2) {
            this.y1 = y1
            this.y2 = y2
        } else {
            this.y2 = y1
            this.y1 = y2
        }
        if (z1 > z2) {
            this.z1 = z1
            this.z2 = z2
        } else {
            this.z2 = z1
            this.z1 = z2
        }
    }

    @BsonIgnore
    Location getMinimum(World world) {
        return new Location(world, this.x2, this.y2, this.z2)
    }

    @BsonIgnore
    Location getMaximum(World world) {
        return new Location(world, this.x1, this.y1, this.z1)
    }

    @BsonIgnore
    int xLength() {
        return (this.x1 - this.x2 + 1)
    }

    @BsonIgnore
    int yLength() {
        return (this.y1 - this.y2 + 1)
    }

    @BsonIgnore
    int zLength() {
        return (this.z1 - this.z2 + 1)
    }

    @BsonIgnore
    int volume() {
        return xLength() * yLength() * zLength()
    }

    @BsonIgnore
    static SR createExact(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        SR region = new SR()
        region.x1 = maxX
        region.y1 = maxY
        region.z1 = maxZ

        region.x2 = minX
        region.y2 = minY
        region.z2 = minZ

        return region
    }


    @Override
    String toString() {
        return "SerializableRegion{" +
                "world='" + world + '\'' +
                ", x1=" + x1 +
                ", y1=" + y1 +
                ", z1=" + z1 +
                ", x2=" + x2 +
                ", y2=" + y2 +
                ", z2=" + z2 +
                '}'
    }
}