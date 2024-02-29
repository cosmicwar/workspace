package scripts.factions.content.worldgen.api

import org.bukkit.Material

class WorldMaterial {
    double chance = -1
    Material material = null

    WorldMaterial(double chance, Material material) {
        this.chance = chance
        this.material = material

    }
}
