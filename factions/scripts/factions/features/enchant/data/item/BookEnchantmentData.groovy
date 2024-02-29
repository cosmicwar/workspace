package scripts.factions.features.enchant.data.item

import com.google.common.collect.Maps
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import groovy.transform.CompileStatic
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import scripts.factions.features.enchant.data.enchant.StoredEnchantment
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.shared.utils.PersistentItemData

@CompileStatic
class BookEnchantmentData extends PersistentItemData {
    private static final NamespacedKey DATA_KEY = new NamespacedKey(Starlight.plugin, "BookEnchantmentData")
    static final Closure<CustomEnchantment> getCustomEnchant = Exports.ptr("enchantments:getEnchantment") as Closure

    final List<StoredEnchantment> storedEnchantments

    double successChance
    double destroyChance

    BookEnchantmentData(List<StoredEnchantment> storedEnchantments, double successChance, double destroyChance) {
        this.storedEnchantments = storedEnchantments
        this.successChance = successChance
        this.destroyChance = destroyChance
    }

    Map<CustomEnchantment, Integer> getStoredEnchantments() {
        Map<CustomEnchantment, Integer> enchantments = Maps.newConcurrentMap()
        storedEnchantments.forEach(storedEnchantment -> {
            def enchant = getCustomEnchant.call(storedEnchantment.getEnchantment()) as CustomEnchantment
            if (enchant == null) {
                return
            }

            enchantments.put(enchant, storedEnchantment.getLevel())
        })

        return enchantments
    }

    List<StoredEnchantment> getInternalEnchantments() {
        return storedEnchantments
    }

    void write(ItemStack itemStack) {
        super.write(itemStack, DATA_KEY)
    }

    static BookEnchantmentData read(ItemStack itemStack) {
        return (BookEnchantmentData) read(itemStack, DATA_KEY, BookEnchantmentData.class)
    }
}