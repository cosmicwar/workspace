package scripts.factions.content.worldgen.api

import org.bukkit.TreeType

class WorldTree {
    double chance = -1
    TreeType treeType = null

    WorldTree(double chance, TreeType treeType) {
        this.chance = chance
        this.treeType = treeType
    }
}
