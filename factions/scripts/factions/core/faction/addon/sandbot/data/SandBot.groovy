package scripts.factions.core.faction.addon.sandbot.data

import com.google.common.collect.Sets
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.starcade.starlight.helper.Schedulers
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.addon.upgrade.UpgradeUtil
import scripts.shared.data.obj.CL
import scripts.shared.data.obj.Position
import scripts.shared.legacy.utils.npc.NPCRegistry
import scripts.shared.legacy.utils.npc.NPCTracker

import java.util.function.Consumer

class SandBot {

    UUID factionId
    int botNumber
    Location location = null

    Set<Location> placeLocations
    long lastCheckMillis

    Boolean spawned = false

    NPCTracker npcTracker = null

    SandBot(UUID factionId, int botNumber) {
        this.factionId = factionId
        this.botNumber = botNumber
        this.placeLocations = Sets.newConcurrentHashSet()
        this.lastCheckMillis = System.currentTimeMillis()
    }

    def spawn(Location location) {
        if (spawned && npcTracker != null) {
            return
        }

        location = getEvenLocation(location)

        this.location = location
        spawned = true
        npcTracker = spawnNpc("${Factions.getFaction(factionId).name}_sb_$botNumber", "bot_$botNumber", location, "bubbleboyhero", { player ->
            placeSand()
        })
    }

    def despawn() {
        if (!spawned || npcTracker == null) {
            return
        }

        spawned = false
        this.location = null
        placeLocations.clear()
        NPCRegistry.get().unregister(npcTracker)
        npcTracker = null
    }

    def placeSand(Material sandMaterial = Material.SAND) {
        if (!spawned || npcTracker == null) {
            return
        }

        if (placeLocations != null && 10_000L < System.currentTimeMillis() - lastCheckMillis) {
            placeLocations.clear()
        }

        Schedulers.sync().execute {
            if (placeLocations.isEmpty()) {
                for (int x = (location.getBlockX() - getFactionRadius()); x <= (location.getBlockX() + getFactionRadius()); x++) {
                    for (int y = (location.getBlockY() - getFactionRadius()); y <= (location.getBlockY() + getFactionRadius()); y++) {
                        for (int z = (location.getBlockZ() - getFactionRadius()); z <= (location.getBlockZ() + getFactionRadius()); z++) {
                            if (y < -63 || y > 320) continue

                            def block = location.world.getBlockAt(x, y, z)

                            if (block.type != Material.SANDSTONE || block.location == location) continue

                            def relativeBlock = block.getRelative(BlockFace.DOWN)

                            if (relativeBlock == null || (relativeBlock.type != Material.AIR && relativeBlock.type != Material.SAND) || relativeBlock == location.getBlock()) continue

                            placeLocations.add(relativeBlock.location)
                        }
                    }
                }

                lastCheckMillis = System.currentTimeMillis()
            }

            double sandPrice = 0 /*placeLocations.size() * shopPrice */
            for (Location placeLocation : placeLocations) {
                def factionAt = Factions.getFactionAt(CL.of(placeLocation))
                if (factionAt == null || factionAt.id != factionId) continue

                def blockUp = placeLocation.getBlock().getRelative(BlockFace.UP)
                if (blockUp.type.isAir()) {
                    placeLocation.getWorld().spawnFallingBlock(placeLocation, sandMaterial, 0 as byte)
                } else {
                    placeLocation.getBlock().setType(sandMaterial)
                }
            }

            if (!placeLocations.isEmpty()) {
                npcTracker.swing()
            }
        }


    }

    int getFactionRadius() {
        def radius = 1

        def upgrade = Factions.getFaction(factionId).upgradeData?.getUpgrade(UpgradeUtil.faction_sandbot_radius)
        if (upgrade != null) {
            radius += upgrade.level
        }

        return radius
    }

    static Location getEvenLocation(Location location) {
        return new Location(location.world, Math.floor(location.x) + 0.5, location.y, Math.floor(location.z) + 0.5, location.yaw, location.pitch)
    }

    static NPCTracker spawnNpc(String id, String name, Location location, Object skinHolder = null, Consumer<Player> onClick = null) {
        NPCTracker npcTracker = NPCRegistry.get().spawn(id, name, location, skinHolder, onClick)
        npcTracker.hand = Material.SAND
        return npcTracker
    }
}
