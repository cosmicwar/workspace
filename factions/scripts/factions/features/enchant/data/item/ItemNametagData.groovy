package scripts.factions.features.enchant.data.item

import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.Starlight
import scripts.shared.utils.PersistentItemData

class ItemNametagData extends PersistentItemData {

    private static final NamespacedKey DATA_KEY = new NamespacedKey(Starlight.plugin, "ItemNametagData")

    ItemNametagData() {
    }

    void write(ItemStack itemStack) {
        super.write(itemStack, DATA_KEY)
    }

    static ItemNametagData read(ItemStack itemStack) {
        return (ItemNametagData) read(itemStack, DATA_KEY, ItemNametagData.class)
    }
}
