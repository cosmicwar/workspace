package scripts.factions.data.obj

import groovy.transform.CompileStatic
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.Chunk
import org.bukkit.Location
import scripts.shared.legacy.utils.BroadcastUtils

@CompileStatic
class CL {

    // CL = ChunkLocation

    String worldName
    int x, z

    CL(){}

    CL(String world, int x, int z) {
        this.worldName = world

        this.x = x
        this.z = z
    }

    @BsonIgnore
    static CL of(Location location) {
        return new CL(location.getWorld().getName(), location.getChunk().getX(), location.getChunk().getZ())
    }

    @BsonIgnore
    static CL of(Chunk chunk) {
        return new CL(chunk.getWorld().getName(), chunk.getX(), chunk.getZ())
    }

    @BsonIgnore
    @Override
    boolean equals(final Object object) {
        if (this.is(object)) {
            return true
        } else if (object == null || this.getClass() != object.getClass()) {
            return false
        }
        CL location = (CL) object
        return location.worldName == this.worldName && location.getX() == this.x && location.getZ() == this.z
    }

    @BsonIgnore
    @Override
    int hashCode() {
        return 31 * (31 * this.x + this.z) + this.worldName.hashCode()
    }

    @BsonIgnore
    @Override
    String toString() {
        return "${this.worldName},${this.x},${this.z}"
    }

    @BsonIgnore
    boolean isEmpty() {
        return false
    }
}
