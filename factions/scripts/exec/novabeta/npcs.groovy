package scripts.exec.novabeta

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.starcade.starlight.helper.Schedulers
import scripts.factions.cfg.WorldConfig
import scripts.shared.legacy.utils.npc.NPCRegistry
import scripts.shared.legacy.utils.npc.NPCTracker

import java.util.function.Consumer

Schedulers.sync().runLater({
    World world = Bukkit.getWorld("${WorldConfig.SPAWN_WORLD_PREFIX}")

    spawnNpc("spawn_tutorial", "§b§lTutorial", new Location(world, -33.5, 180, -5.5, 44.5, 10.5), "Abugatti", { Player player ->
        player.performCommand("tutorial")
    })

    spawnNpc("spawn_armorsets", "§b§lCustom Sets", new Location(world, -33.5, 180, 6.5, 135.5, 15.5), "ukwifi", { Player player ->
        player.performCommand("sets")
    })
}, 10L)

static NPCTracker spawnNpc(String id, String name, Location location, Object skinHolder = null, Consumer<Player> onClick = null) {
    NPCTracker npcTracker = NPCRegistry.get().spawn(id, name, location, skinHolder, onClick)
    npcTracker.turnTowardPlayers = true
    return npcTracker
}