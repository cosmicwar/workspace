package scripts.factions.features.enchant.data.item

import org.starcade.starlight.Starlight
import groovy.transform.CompileStatic
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import scripts.shared.utils.PersistentItemData

@CompileStatic
class TransmogScrollData extends PersistentItemData {

    private static final NamespacedKey DATA_KEY = new NamespacedKey(Starlight.plugin, "TransmogScrollData")

    TransmogScrollData() { }

    void write(ItemStack itemStack) {
        super.write(itemStack, DATA_KEY)
    }

    static TransmogScrollData read(ItemStack itemStack) {
        return (TransmogScrollData) read(itemStack, DATA_KEY, TransmogScrollData.class)
    }

}

