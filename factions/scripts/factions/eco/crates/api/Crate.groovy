package scripts.factions.eco.crates.api

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.persistence.PersistentDataType
import org.starcade.starlight.helper.Schedulers
import scripts.shared.data.obj.Position
import scripts.shared.data.uuid.UUIDDataObject
import scripts.factions.eco.crates.Crates
import scripts.factions.eco.loottable.LootTableHandler
import scripts.factions.eco.loottable.v2.api.LootTable
import scripts.shared.utils.DataUtils

@CompileStatic(TypeCheckingMode.SKIP)
class Crate extends UUIDDataObject {

    String internalName

    Set<Position> placedLocations = []

    String crateKeyName = ""
    List<String> crateKeyLore = []
    Material crateKeyMaterial = Material.PAPER
    int crateKeyCustomModelData = 0

    UUID lootTableId = null

    @BsonIgnore transient Map<Location, PlacedCrate> placedCrates = [:]

    Crate() {}

    Crate(UUID uuid) {
        super(uuid)
    }

    Crate(UUID uuid, String internalName) {
        super(uuid)

        this.internalName = internalName
    }

    @BsonIgnore
    def loadCrates() {
        placedLocations.each {
            def world = Bukkit.getWorld(it.world)
            if (world != null) {
                placeCrate(it.getLocation(world))
            }
        }
    }

    @BsonIgnore
    def placeCrate(Location location) {
        if (placedCrates.containsKey(location)) return

        PlacedCrate placedCrate = new PlacedCrate(this, location)
        placedCrates[location] = placedCrate

        placedCrate.spawnHologram([
                "&e&l${internalName}"
        ])

        Schedulers.sync().execute {
            if (location.block.type != Material.CHEST) {
                location.block.type = Material.CHEST

                def chest = location.block as Chest
                DataUtils.setTag(chest, Crates.placedCrateKey, PersistentDataType.STRING, id.toString())
            }
        }

        def ps = Position.of(location)
        if (!placedLocations.contains(ps)) placedLocations << ps
    }

    @BsonIgnore
    def removeCrate(Location location) {
        if (!placedCrates.containsKey(location)) return

        placedCrates.remove(location)

        def ps = Position.of(location)
        placedLocations.remove(ps)
    }

    @BsonIgnore
    LootTable getLootTable() {
        return LootTableHandler.getLootTable(lootTableId)
    }

    @BsonIgnore @Override
    boolean isEmpty() {
        return false
    }
}
