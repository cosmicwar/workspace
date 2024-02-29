package scripts.factions.eco.crates.api

import org.bukkit.Location
import scripts.shared.features.holograms.HologramRegistry
import scripts.shared.features.holograms.HologramTracker

class PlacedCrate {

    UUID id = UUID.randomUUID()

    Crate crate
    Location location

    HologramTracker hologram = null

    PlacedCrate(Crate crate, Location location) {
        this.crate = crate
        this.location = location
    }

    def spawnHologram(List<String> text) {
        if (hologram != null) {
            updateHologram(text)
        } else {
            hologram = HologramRegistry.get().spawn("crate_${getId()}", location.clone().add(0.5D, .95D, 0.5D), text, false)
        }
    }

    def removeHologram() {
        HologramRegistry.get().unregister(hologram)
        hologram = null
    }

    def updateHologram(List<String> text) {
        hologram?.updateLines(text)
    }

}
