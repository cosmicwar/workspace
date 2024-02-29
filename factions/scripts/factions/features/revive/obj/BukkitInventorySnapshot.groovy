package scripts.factions.features.revive.obj

import com.google.common.collect.Maps
import org.bukkit.inventory.ItemStack
import scripts.factions.data.obj.Position
import scripts.shared.legacy.utils.FastItemUtils

class BukkitInventorySnapshot {

    UUID snapshotId = UUID.randomUUID()

    Map<Integer, ItemStack> inventorySlots = Maps.newConcurrentMap()

    ItemStack heldItem = null

    ItemStack helmet = null
    ItemStack chestPlate = null
    ItemStack leggings = null
    ItemStack boots = null

    Position position = null

    Long timeStamp = null

    int timesRevived = 0
    Long lastRevive = null

    String deathCause = ""
    UUID killedBy = null

    BukkitInventorySnapshot(InventorySnapshot snapshot) {
        this.snapshotId = snapshot.snapshotId

        snapshot.inventorySlots.each {
            inventorySlots.put(it.slot, FastItemUtils.convertStringToItemStack(it.item))
        }

        if (snapshot.heldItem != null) this.heldItem = FastItemUtils.convertStringToItemStack(snapshot.heldItem)

        if (snapshot.helmet != null) this.helmet = FastItemUtils.convertStringToItemStack(snapshot.helmet)
        if (snapshot.chestPlate != null) this.chestPlate = FastItemUtils.convertStringToItemStack(snapshot.chestPlate)
        if (snapshot.leggings != null) this.leggings = FastItemUtils.convertStringToItemStack(snapshot.leggings)
        if (snapshot.boots != null) this.boots = FastItemUtils.convertStringToItemStack(snapshot.boots)

        if (snapshot.position.world != null) this.position = snapshot.position

        if (snapshot.timeStamp != null) this.timeStamp = snapshot.timeStamp
        if (snapshot.timesRevived != 0) this.timesRevived = snapshot.timesRevived

        this.lastRevive = snapshot.lastRevive

        this.deathCause = snapshot.deathCause
        this.killedBy = snapshot.killedBy
    }

}
