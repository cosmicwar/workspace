package scripts.factions.features.enchant.data.item

import org.starcade.starlight.Starlight
import groovy.transform.CompileStatic
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import scripts.factions.features.enchant.items.EnchantmentOrbType
import scripts.shared.utils.PersistentItemData

@CompileStatic
class EnchantmentOrbData extends PersistentItemData {
    private static final NamespacedKey DATA_KEY = new NamespacedKey(Starlight.plugin, "EnchantmentOrbData")

    final int successRate
    final int slotIncrease
    final EnchantmentOrbType enchantmentOrbType

    EnchantmentOrbData(int successRate, int slotIncrease, EnchantmentOrbType enchantmentOrbType) {
        this.successRate = successRate
        this.slotIncrease = slotIncrease
        this.enchantmentOrbType = enchantmentOrbType
    }

    void write(ItemStack itemStack) {
        super.write(itemStack, DATA_KEY)
    }

    static EnchantmentOrbData read(ItemStack itemStack) {
        return (EnchantmentOrbData) read(itemStack, DATA_KEY, EnchantmentOrbData.class)
    }
}

