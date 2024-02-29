package scripts.factions.features.enchant.data.item

import org.starcade.starlight.Starlight
import groovy.transform.CompileStatic
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.shared.utils.PersistentItemData

@CompileStatic
class EnchantmentDustData extends PersistentItemData {

    private static final NamespacedKey DATA_KEY = new NamespacedKey(Starlight.plugin, "EnchantmentDustData")

    final EnchantmentTier enchantmentTier
    final int successIncrease

    EnchantmentDustData(EnchantmentTier enchantmentTier, int successIncrease) {
        this.enchantmentTier = enchantmentTier
        this.successIncrease = successIncrease
    }

    void write(ItemStack itemStack) {
        super.write(itemStack, DATA_KEY)
    }

    static EnchantmentDustData read(ItemStack itemStack) {
        return (EnchantmentDustData) read(itemStack, DATA_KEY, EnchantmentDustData.class)
    }
}

