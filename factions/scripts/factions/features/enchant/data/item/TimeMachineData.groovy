package scripts.factions.features.enchant.data.item

import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.Starlight
import scripts.shared.utils.PersistentItemData

class TimeMachineData extends PersistentItemData {
    private static final NamespacedKey DATA_KEY = new NamespacedKey(Starlight.plugin, "TimeMachineData")

    boolean used

    TimeMachineData() {
        this.used = false
    }

    void write(ItemStack itemStack) {
        super.write(itemStack, DATA_KEY)
    }

    static TimeMachineData read(ItemStack itemStack) {
        return (TimeMachineData) read(itemStack, DATA_KEY, TimeMachineData.class)
    }
}
