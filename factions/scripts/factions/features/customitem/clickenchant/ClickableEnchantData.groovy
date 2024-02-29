package scripts.factions.features.customitem.clickenchant

import org.starcade.starlight.Starlight
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import scripts.shared.utils.PersistentItemData

class ClickableEnchantData extends PersistentItemData {

    private static final NamespacedKey DATA_KEY = new NamespacedKey(Starlight.plugin, "ClickableEnchantData")

    final ClickableEnchant clickableEnchant

    final int level
    final boolean unsafe

    ClickableEnchantData(ClickableEnchant clickableEnchant, int level, boolean unsafe) {
        this.clickableEnchant = clickableEnchant
        this.level = level
        this.unsafe = unsafe
    }

    void write(ItemStack itemStack) {
        super.write(itemStack, DATA_KEY)
    }

    static ClickableEnchantData read(ItemStack itemStack) {
        return (ClickableEnchantData) read(itemStack, DATA_KEY, ClickableEnchantData.class)
    }

}

