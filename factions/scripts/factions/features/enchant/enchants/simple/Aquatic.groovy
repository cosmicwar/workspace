package scripts.factions.features.enchant.enchants.simple

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.shared.utils.ItemType


class Aquatic extends CustomEnchantment {
    Aquatic() {
        super(
                "aquatic",
                EnchantmentTier.SIMPLE,
                EnchantmentType.NORMAL,
                "Aquatic",
                ["Allows you to breathe underwater."],
                [ItemType.HELMET],
                1
        )
    }

    @Override
    void onEquip(Player player, ItemStack itemStack, int enchantLevel) {
        addPotionEffect(player, PotionEffectType.WATER_BREATHING, 1)
    }

    @Override
    void onUnequip(Player player, ItemStack itemStack, int enchantLevel) {
        removePotionEffect(player, PotionEffectType.WATER_BREATHING, 1)
    }
}
