package scripts.factions.content.worldgen.schem.nms

import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.level.chunk.LevelChunkSection
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_20_R1.CraftChunk
import org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.scheduler.Task
import scripts.factions.content.worldgen.schem.RegionTransformer
import scripts.shared.legacy.objects.Region

class RegionTransformer17 extends RegionTransformer {

    @Override
    void set(World world, Region region, Material material, int ticks = region.yLength().intdiv(20) + 1) {
        BlockState data = CraftBlockData.newData(material, null).getState()

        int volume = region.volume()
        int blocksPerTick = Math.ceil(volume / ticks as double) as int

        int tick = 0

        int startX = region.x2
        int startY = region.y1
        int startZ = region.z2

        int endX = region.x1
        int endY = region.y2
        int endZ = region.z1

        int x = startX
        int y = startY
        int z = startZ

        Task task

        task = Schedulers.sync().runRepeating({
            int changed = 0

            for (; x <= endX; ++x) {
                int cx = x >> 4

                for (; z <= endZ; ++z) {
                    int cz = z >> 4

                    CraftChunk craftChunk = world.getChunkAt(cx, cz) as CraftChunk
                    LevelChunk chunk = craftChunk.getHandle()

                    for (; y >= endY; --y) {
                        setType(chunk, x, y, z, data)

                        if (++changed >= blocksPerTick) {
                            if (--y < endY) {
                                y = startY
                            }
                            ++tick
                            return
                        }
                    }
                    y = startY
                }
                z = startZ
            }
            x = startX

            if (changed + (tick++ * blocksPerTick) >= volume || changed == 0) {
                task.stop()
            }
        }, 0, 2)
    }

    @Override
    void set(Block block, Material material) {
        setType((block.getChunk() as CraftChunk).getHandle(), block.getX(), block.getY(), block.getZ(), CraftBlockData.newData(material, null).getState())
    }

    private static void setType(LevelChunk chunk, int x, int y, int z, BlockState data) {
        LevelChunkSection section = chunk.sections[y >> 4]

        if (section == LevelChunk.EMPTY_SECTION) {
            if (data.isAir()) {
                return
            }
            section = new LevelChunkSection((y >> 4) << 4, chunk, chunk.level, true)
            chunk.sections[y >> 4] = section

        }
        section.getStates().set(x & 15, y & 15, z & 15, data)

        if (chunk.playerChunk != null) {
            chunk.playerChunk.blockChanged(new net.minecraft.core.BlockPos(x & 15, y, z & 15))
        }
        chunk.markUnsaved()
    }
}
