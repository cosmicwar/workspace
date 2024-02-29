package scripts.factions.features.enchant.enchants.legendary

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.shared.utils.ItemType


class Overload extends CustomEnchantment {

    Overload() {
        super(
                "overload",
                EnchantmentTier.LEGENDARY,
                EnchantmentType.NORMAL,
                "Overload",
                ["Increases your maximum health."],
                [ItemType.HELMET, ItemType.CHESTPLATE, ItemType.LEGGINGS, ItemType.BOOTS],
                3
        )
    }

    @Override
    void onEquip(Player player, ItemStack itemStack, int enchantLevel) {
        addPotionEffect(player, PotionEffectType.HEALTH_BOOST, enchantLevel - 1)

    }

    @Override
    void onUnequip(Player player, ItemStack itemStack, int enchantLevel) {
        removePotionEffect(player, PotionEffectType.HEALTH_BOOST, enchantLevel - 1)
    }
}
