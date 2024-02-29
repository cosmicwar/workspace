package scripts.factions.data.obj

import groovy.transform.CompileStatic
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Player
import scripts.shared.legacy.utils.IntegerUtils

import java.util.function.Consumer

@CompileStatic
class Region {
    public int x1, y1, z1, x2, y2, z2
    transient public World world

    Region() {}

    Region(int x1, int y1, int z1, int x2, int y2, int z2, World world = null) {
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
        this.world = world
    }

    boolean contains(int x, int y, int z, boolean checkWorld = false) {
        if (checkWorld && this.world != world) return false
        return (this.x2 <= x && x <= this.x1) && (this.y2 <= y && y <= this.y1) && (this.z2 <= z && z <= this.z1)
    }

    boolean contains(double x, double y, double z, boolean checkWorld = false) {
        if (checkWorld && this.world != world) return false
        return (this.x2 <= x && x <= this.x1) && (this.y2 <= y && y <= this.y1) && (this.z2 <= z && z <= this.z1)
    }

    boolean contains(Location loc, boolean checkWorld = false) {
        return contains(loc.getX(), loc.getY(), loc.getZ(), checkWorld)
    }

    boolean contains(Block block, boolean checkWorld = false) {
        return contains(block.getLocation(), checkWorld)
    }

    boolean containsHorizontal(int x, int z) {
        return (this.x2 <= x && x <= this.x1) && (this.z2 <= z && z <= this.z1)
    }

    boolean containsHorizontal(double x, double z) {
        return (this.x2 <= x && x <= this.x1) && (this.z2 <= z && z <= this.z1)
    }

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

    Region expand(int x, int y, int z) {
        return new Region(this.x1 + x, this.y1 + y, this.z1 + z, this.x2 - x, this.y2 - y, this.z2 - z)
    }

    Region copy() {
        return new Region(this.x1, this.y1, this.z1, this.x2, this.y2, this.z2)
    }

    @BsonIgnore
    Location getMinimum(World world) {
        return new Location(world, this.x2, this.y2, this.z2)
    }

    @BsonIgnore
    Location getMaximum(World world) {
        return new Location(world, this.x1, this.y1, this.z1)
    }

    int xLength() {
        return (this.x1 - this.x2 + 1)
    }

    int yLength() {
        return (this.y1 - this.y2 + 1)
    }

    int zLength() {
        return (this.z1 - this.z2 + 1)
    }

    int volume() {
        return xLength() * yLength() * zLength()
    }

    void loadChunks(World world) {
        this.world = world
    }

    @BsonIgnore
    List<Player> getPlayers() {
        List<Player> players = new ArrayList<>()

        for (Player player : this.world.getPlayers()) {
            if (this.contains(player.getLocation())) {
                players.add(player)
            }
        }
        return players
    }

    void blockPositions(Consumer<BlockPosition> consumer) {
        int minX = Math.min(x1, x2)
        int minY = Math.min(y1, y2)
        int minZ = Math.min(z1, z2)

        int maxX = Math.max(x1, x2)
        int maxY = Math.max(y1, y2)
        int maxZ = Math.max(z1, z2)

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    consumer.accept(new BlockPosition(x, y, z))
                }
            }
        }
    }

    @Override
    String toString() {
        return "${this.x1},${this.y1},${this.z1},${this.x2},${this.y2},${this.z2}"
    }

    static Region createExact(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        Region region = new Region()
        region.x1 = maxX
        region.y1 = maxY
        region.z1 = maxZ

        region.x2 = minX
        region.y2 = minY
        region.z2 = minZ

        return region
    }

    static Region fromString(String string) {
        String[] data = string.split(",")
        int x1 = IntegerUtils.unsafelyParseInt(data[0])
        int y1 = IntegerUtils.unsafelyParseInt(data[1])
        int z1 = IntegerUtils.unsafelyParseInt(data[2])
        int x2 = IntegerUtils.unsafelyParseInt(data[3])
        int y2 = IntegerUtils.unsafelyParseInt(data[4])
        int z2 = IntegerUtils.unsafelyParseInt(data[5])
        return new Region(x1, y1, z1, x2, y2, z2)
    }
}