package scripts.factions.features.enchant.data.item

import org.starcade.starlight.Starlight
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import scripts.shared.utils.PersistentItemData

class BlackScrollData extends PersistentItemData {
    private static final NamespacedKey DATA_KEY = new NamespacedKey(Starlight.plugin, "BlackScrollData")

    final int newSuccessRate

    BlackScrollData(int newSuccessRate) {
        this.newSuccessRate = newSuccessRate
    }

    void write(ItemStack itemStack) {
        super.write(itemStack, DATA_KEY)
    }

    static BlackScrollData read(ItemStack itemStack) {
        return (BlackScrollData) read(itemStack, DATA_KEY, BlackScrollData.class)
    }
}