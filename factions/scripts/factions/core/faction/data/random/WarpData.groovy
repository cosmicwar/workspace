package scripts.factions.core.faction.data.random

import org.bukkit.Material
import scripts.shared.data.obj.Position

class WarpData {

    Material icon = Material.GRASS_BLOCK

    String name
    Position position

    WarpData() {
    }

    WarpData(String name, Position position) {
        this.name = name
        this.position = position
    }

    WarpData(String name, Position position, Material icon) {
        this.name = name
        this.position = position
        this.icon = icon
    }


}

