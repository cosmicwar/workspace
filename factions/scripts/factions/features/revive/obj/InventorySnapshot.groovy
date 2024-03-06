package scripts.factions.features.revive.obj

import com.google.common.collect.Sets
import scripts.shared.data.obj.Position

class InventorySnapshot {

    UUID snapshotId = UUID.randomUUID()

    Set<SlotSnapshot> inventorySlots = Sets.newConcurrentHashSet()

    String heldItem = null

    String helmet = null
    String chestPlate = null
    String leggings = null
    String boots = null

    Position position = null

    Long timeStamp = null

    int timesRevived = 0
    Long lastRevive = null

    String deathCause = ""
    UUID killedBy = null

    InventorySnapshot() {}

}
