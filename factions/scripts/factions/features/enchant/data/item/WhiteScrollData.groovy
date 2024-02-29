package scripts.factions.features.enchant.data.item

import org.starcade.starlight.Starlight
import groovy.transform.CompileStatic
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import scripts.shared.utils.PersistentItemData

@CompileStatic
class WhiteScrollData extends PersistentItemData {

    Boolean whiteScroll

    WhiteScrollData(Boolean whiteScroll = true) {
        this.whiteScroll = whiteScroll
    }
}

