package scripts.factions.data.obj

import org.bukkit.Location
import org.bukkit.Material
import scripts.shared.legacy.utils.IntegerUtils

class UniformMineRegion extends Region {
    protected Material[][][] blocks

    UniformMineRegion(int x1, int y1, int z1, int x2, int y2, int z2) {
        super(x1, y1, z1, x2, y2, z2)

        this.blocks = new Material[this.xLength() + 1][this.yLength() + 1][this.zLength() + 1]
    }

    void setType(Material material) {
        if (material == null) {
            println "GLOBAL TYPE NULL"
        }

        int xLength = this.xLength() + 1
        int yLength = this.yLength() + 1
        int zLength = this.zLength() + 1

        for (int x = 0; x < xLength; ++x) {
            for (int y = 0; y < yLength; ++y) {
                for (int z = 0; z < zLength; ++z) {
                    this.blocks[x][y][z] = material
                }
            }
        }
    }

    void setType(int x, int y, int z, Material material) {
        if (material == null) {
            println "LOCAL TYPE NULL"
        }
        this.blocks[x - this.x2][y - this.y2][z - this.z2] = material
    }

    void setType(Location location, Material material) {
        this.setType(location.getBlockX(), location.getBlockY(), location.getBlockZ(), material)
    }

    Material getType(int x, int y, int z) {
        return this.blocks[x - this.x2][y - this.y2][z - this.z2]
    }

    Material getType(Location location) {
        return this.getType(location.getBlockX(), location.getBlockY(), location.getBlockZ())
    }

    static UniformMineRegion fromString(String string) {
        String[] data = string.split(",")
        int x1 = IntegerUtils.unsafelyParseInt(data[0])
        int y1 = IntegerUtils.unsafelyParseInt(data[1])
        int z1 = IntegerUtils.unsafelyParseInt(data[2])
        int x2 = IntegerUtils.unsafelyParseInt(data[3])
        int y2 = IntegerUtils.unsafelyParseInt(data[4])
        int z2 = IntegerUtils.unsafelyParseInt(data[5])
        return new UniformMineRegion(x1, y1, z1, x2, y2, z2)
    }
}