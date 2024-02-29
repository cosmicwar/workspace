package scripts.factions.features.enchant.data.item

import org.starcade.starlight.Starlight
import groovy.transform.CompileStatic
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import scripts.shared.utils.PersistentItemData

@CompileStatic
class WhiteScrollItemData extends PersistentItemData {
    private static final NamespacedKey DATA_KEY = new NamespacedKey(Starlight.plugin, "WhiteItemScrollData")

    boolean holyWhiteScroll

    WhiteScrollItemData(boolean holyWhiteScroll = false) {
        this.holyWhiteScroll = holyWhiteScroll
    }

    void write(ItemStack itemStack) {
        super.write(itemStack, DATA_KEY)
    }

    static WhiteScrollItemData read(ItemStack itemStack) {
        return (WhiteScrollItemData) read(itemStack, DATA_KEY, WhiteScrollItemData.class)
    }
}

