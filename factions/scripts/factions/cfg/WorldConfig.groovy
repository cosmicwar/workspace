package scripts.factions.cfg

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World

class WorldConfig {

    static Location spawn

    static String SPAWN_WORLD_PREFIX = "starcade"

    static Location getSpawn() {
        World world = Bukkit.getWorld("${SPAWN_WORLD_PREFIX}")
        def loc = spawn.clone()
        loc.setWorld(world)
        return loc
    }
}
