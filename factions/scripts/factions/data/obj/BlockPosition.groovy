package scripts.factions.data.obj

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import scripts.shared.legacy.utils.IntegerUtils

class BlockPosition {

    int x, y, z, chunkX, chunkZ, hash
    String world

    BlockPosition(int x, int y, int z) {
        this.x = x
        this.y = y
        this.z = z

        this.hash = 31 * (31 * this.x + this.y) + this.z
    }

    BlockPosition(Location location) {
        this(location.getBlockX(), location.getBlockY(), location.getBlockZ())
    }

    Location getLocation() {
        return new Location(Bukkit.getWorld(world), this.x, this.y, this.z)
    }

//    Location getLocation(String world) {
//        return getLocation(Bukkit.getWorld(world))
//    }

    BlockPosition add(BlockPosition position) {
        return new BlockPosition(this.x + position.x, this.y + position.y, this.z + position.z)
    }

    void valueOf(Location location) {
        this.x = location.getBlockX()
        this.y = location.getBlockY()
        this.z = location.getBlockZ()
        this.chunkX = location.getChunk().x
        this.chunkZ = location.getChunk().z

        this.hash = 31 * (31 * this.x + this.y) + this.z
    }

    @Override
    boolean equals(Object object) {
        if (this.is(object)) {
            return true
        }
        if (!(object instanceof BlockPosition)) {
            return false
        }
        BlockPosition location = (BlockPosition) object
        return location.x == this.x && location.y == this.y && location.z == this.z
    }

    @Override
    int hashCode() {
        return this.hash
    }

    static BlockPosition fromString(String string) {
        final String[] split = string.split(",")
        int x = IntegerUtils.unsafelyParseInt(split[0])
        int y = IntegerUtils.unsafelyParseInt(split[1])
        int z = IntegerUtils.unsafelyParseInt(split[2])
        return new BlockPosition(x, y, z)
    }
}