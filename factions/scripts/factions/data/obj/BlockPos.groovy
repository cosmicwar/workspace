package scripts.factions.data.obj

import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import scripts.shared.legacy.utils.IntegerUtils

class BlockPos {
    int x, y, z, hash
    private String string

    BlockPos() {
    }

    BlockPos(int x, int y, int z, String string) {
        this.x = x
        this.y = y
        this.z = z
        this.string = string

        this.hash = 31 * (31 * this.x + this.y) + this.z
    }

    BlockPos(int x, int y, int z) {
        this(x, y, z, "${x},${y},${z}")
    }

    BlockPos(Location location) {
        this(location.getBlockX(), location.getBlockY(), location.getBlockZ())
    }

    BlockPos(Block block) {
        this(block.getX(), block.getY(), block.getZ())
    }

    @BsonIgnore
    Location getLocation(World world) {
        return new Location(world, this.x, this.y, this.z)
    }

    @BsonIgnore
    Location getLocation(String world) {
        return getLocation(Bukkit.getWorld(world))
    }

    BlockPos add(BlockPos position) {
        return new BlockPos(this.x + position.x, this.y + position.y, this.z + position.z)
    }

    @Override
    boolean equals(Object object) {
        if (this.is(object)) {
            return true
        }
        if (!(object instanceof BlockPos)) {
            return false
        }
        BlockPos location = (BlockPos) object
        return location.x == this.x && location.y == this.y && location.z == this.z
    }

    @Override
    int hashCode() {
        return this.hash
    }

    @Override
    String toString() {
        return this.string
    }

    static BlockPos fromString(String string) {
        if (string == null || string.isEmpty()) return null

        final String[] split = string.split(",")
        int x = IntegerUtils.unsafelyParseInt(split[0])
        int y = IntegerUtils.unsafelyParseInt(split[1])
        int z = IntegerUtils.unsafelyParseInt(split[2])
        return new BlockPos(x, y, z, string)
    }
}