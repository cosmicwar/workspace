package scripts.factions.events.envoy

import org.bukkit.Location
import org.bukkit.Material
import org.starcade.starlight.helper.Schedulers
import scripts.shared.features.holograms.HologramRegistry
import scripts.shared.features.holograms.HologramTracker
import scripts.shared.visuals.floating.FloatingBlock

class Envoy {

    UUID id = UUID.randomUUID()

    Location location = null
    boolean falling = false

    HologramTracker hologram = null
    FloatingBlock block = null

    def spawn(Location location) {
        killBlock()

        falling = true
        this.location = location

        block = new FloatingBlock(location.world, location, Material.BEACON)
        block.track()

        spawnHologram()
    }

    def spawnChest(Location location) {
        falling = false
        this.location = location

        Schedulers.sync().execute {
            location.getBlock().setType(Material.CHEST)
        }
    }

    def killChest() {
        Schedulers.sync().execute {
            location.getBlock().setType(Material.AIR)
            removeHologram()
        }
    }

    def killBlock() {
        falling = false

        block?.untrack()
        block = null
    }

    def tick() {
        if (block != null) {
            if (hologram == null) spawnHologram()

            if (falling) {
                if (block.currentLocation.clone().add(0, -1, 0).block.type.isAir()) {
                    block.move(0, -1, 0)
                    hologram.moveTo(hologram.location.clone().add(0, -1, 0))
                } else {
                    falling = false
                    spawnChest(block.currentLocation)
                }
            }
        }
    }

    def spawnHologram() {
        if (hologram != null) return

        hologram = HologramRegistry.get().spawn("envoy_${id}", location.clone().add(0.5D, 2D, 0.5D), [
                "§3envoy"
        ] as List<String>, false)
    }

    def removeHologram() {
        HologramRegistry.get().unregister(hologram)
        hologram = null
    }

}
