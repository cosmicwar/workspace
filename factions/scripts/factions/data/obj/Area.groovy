package scripts.factions.data.obj

import org.bukkit.Location
import org.bukkit.entity.Player

class Area {

    double minX, minY, minZ, maxX, maxY, maxZ

    Area(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.minX = Math.min(minX, maxX)
        this.minY = Math.min(minY, maxY)
        this.minZ = Math.min(minZ, maxZ)
        this.maxX = Math.max(minX, maxX)
        this.maxY = Math.max(minY, maxY)
        this.maxZ = Math.max(minZ, maxZ)
    }

    Area offset(double x, double y, double z) {
        return new Area(minX + x, minY + y, minZ + z, maxX + x, maxY + y, maxZ + z)
    }

    boolean contains(Player player) {
        return contains(player.location)
    }

    boolean contains(Location location) {
        return location.getX() >= minX && location.getX() <= maxX && location.getY() >= minY && location.getY() <= maxY && location.getZ() >= minZ && location.getZ() <= maxZ
    }

    @Override
    String toString() {
        return "${minX},${minY},${minZ},${maxX},${maxY},${maxZ}"
    }

}
