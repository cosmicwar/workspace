package scripts.factions.content.worldgen.schem

import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import scripts.shared.legacy.objects.Region

abstract class RegionTransformer {
    abstract void set(World world, Region region, Material material, int ticks = region.yLength().intdiv(20) + 1)
    abstract void set(Block block, Material material)
}