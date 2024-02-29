package scripts.factions.data.obj

import com.google.common.collect.Sets
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import scripts.factions.features.revive.obj.SlotSnapshot

@CompileStatic(TypeCheckingMode.SKIP)
class SInventory {

    Set<SlotSnapshot> inventorySlots = Sets.newConcurrentHashSet()

    String heldItem = null

    String helmet = null
    String chestPlate = null
    String leggings = null
    String boots = null

    SInventory() {}

}
