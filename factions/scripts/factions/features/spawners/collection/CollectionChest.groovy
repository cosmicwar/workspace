package scripts.factions.features.spawners.collection

import com.google.common.collect.Sets
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.Bukkit
import org.bukkit.World
import scripts.factions.data.DataObject
import scripts.shared.features.holograms.HologramRegistry
import scripts.shared.features.holograms.HologramTracker
import scripts.shared.legacy.objects.Position

@CompileStatic(TypeCheckingMode.SKIP)
class CollectionChest extends DataObject
{
    String chestId

    String world = ""
    Position position = new Position()

    Set<CollectionChestEntry> entries = Sets.newConcurrentHashSet()

    boolean hologramEnabled = true

    @BsonIgnore
    transient HologramTracker hologram = null

    CollectionChest() {}

    CollectionChest(String chestId)
    {
        this.chestId = chestId
    }

    CollectionChest(String chestId, double x, double y, double z, World world)
    {
        this.chestId = chestId

        this.position.x = x
        this.position.y = y
        this.position.z = z

        this.world = world.getName()
    }

    CollectionChest(double x, double y, double z, World world)
    {
        this("$x:$y:$z:$world.name", x, y, z, world)
    }

    CollectionChest(Position position, World world)
    {
        this.chestId = "$position.x:$position.y:$position.z:$world.name"
        this.world = world.getName()
        this.position = position
    }

    @BsonIgnore
    void addEntry(CollectionChestEntry entry)
    {
        if (entry == null) return

        if (this.entries.contains(entry)) return

        this.entries.add(entry)

    }

    @BsonIgnore
    List<CollectionChestEntry> getEntriesList()
    {
        return this.entries.toList()
    }

    @BsonIgnore
    double getValue()
    {
        return (this.entries.collect { it.getTotalSellAmount() }.sum() ?: 0.0D) as double
    }

    @BsonIgnore
    def spawnHologram(boolean force = true) {
        if (!hologramEnabled && !force) return

        if (hologram != null && hologramEnabled) {
            updateHologram()
            return
        }

        hologram = HologramRegistry.get().spawn("cc_${getId()}", position.getLocation(Bukkit.getWorld(world)).clone().add(0.5D, .95D, 0.5D), [
                "§3§lCollection Chest",
                "§eValue: §a§l§n\$${getValue()}",
        ] as List<String>, false)
        hologramEnabled = true
    }

    @BsonIgnore
    def removeHologram(boolean unload = false) {
        HologramRegistry.get().unregister(hologram)
        hologram = null

        if (!unload) hologramEnabled = false
    }

    @BsonIgnore
    def updateHologram() {
        if (!hologramEnabled) {
            removeHologram()
        } else {
            hologram?.updateLines([
                "§3§lCollection Chest",
                "§eValue: §a§l§n\$${getValue()}",
            ] as List<String>)
        }
    }

    @BsonIgnore
    @Override
    boolean isEmpty() {
        return false
    }
}

