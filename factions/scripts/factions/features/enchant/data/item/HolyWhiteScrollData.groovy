package scripts.factions.features.enchant.data.item

import org.starcade.starlight.Starlight
import groovy.transform.CompileStatic
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import scripts.shared.utils.PersistentItemData

@CompileStatic
class HolyWhiteScrollData {

    Integer holiesApplied
    Boolean hasHoly

    HolyWhiteScrollData(Boolean hasHoly = true) {
        this.hasHoly = hasHoly
        this.holiesApplied = hasHoly ? 1 : 0
    }
}

