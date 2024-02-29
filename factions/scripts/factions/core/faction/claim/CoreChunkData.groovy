package scripts.factions.core.faction.claim

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.Bukkit
import scripts.factions.core.faction.Factions
import scripts.factions.data.obj.CL
import scripts.factions.data.obj.Position
import scripts.shared.features.holograms.HologramRegistry
import scripts.shared.features.holograms.HologramTracker

@CompileStatic(TypeCheckingMode.SKIP)
class CoreChunkData {

    UUID factionId = null

    CL chunkLocation = new CL()

    Position blockPosition = new Position()

    @BsonIgnore
    transient HologramTracker blockHologram = null

    CoreChunkData() {}

    CoreChunkData(UUID factionId, CL chunkLocation, Position position) {
        this.factionId = factionId
        this.chunkLocation = chunkLocation
        this.blockPosition = position
    }

    @BsonIgnore
    def spawnHologram() {
        if (blockHologram != null || factionId == null || !chunkLocation.worldName || !blockPosition.world) {
            destroyHologram()
        }

        def faction = Factions.getFaction(factionId, false)
        if (faction == null) return

        blockHologram = HologramRegistry.get().spawn("${factionId.toString()}_core", blockPosition.getLocation(Bukkit.getWorld(chunkLocation.worldName)).clone().add(0.5D, .95D, 0.5D), [
                "§a§l${faction.name} §b§lCore",
                "§a\$1,000",
        ], true)
    }

    @BsonIgnore
    def destroyHologram() {
        if (blockHologram == null) return

        HologramRegistry.get().unregister(blockHologram)
        blockHologram = null
    }

}

