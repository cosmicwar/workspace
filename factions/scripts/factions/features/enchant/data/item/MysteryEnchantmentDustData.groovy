package scripts.factions.features.enchant.data.item

import org.starcade.starlight.Starlight
import groovy.transform.CompileStatic
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.shared.utils.PersistentItemData

@CompileStatic
class MysteryEnchantmentDustData extends PersistentItemData {
    private static final NamespacedKey DATA_KEY = new NamespacedKey(Starlight.plugin, "MysteryEnchantmentDustData")

    final EnchantmentTier enchantmentTier

    MysteryEnchantmentDustData(EnchantmentTier enchantmentTier) {
        this.enchantmentTier = enchantmentTier
    }

    void write(ItemStack itemStack) {
        super.write(itemStack, DATA_KEY)
    }

    static MysteryEnchantmentDustData read(ItemStack itemStack) {
        return (MysteryEnchantmentDustData) read(itemStack, DATA_KEY, MysteryEnchantmentDustData.class)
    }
}

