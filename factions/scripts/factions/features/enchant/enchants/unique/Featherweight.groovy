package scripts.factions.features.enchant.enchants.unique

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.shared.utils.ItemType

class Featherweight extends CustomEnchantment {
    Featherweight() {
        super(
                "haste",
                EnchantmentTier.UNIQUE,
                EnchantmentType.NORMAL,
                "Haste",
                ["Increases your mining speed."],
                [ItemType.SWORD],
                3
        )
    }

    @Override
    void onEquip(Player player, ItemStack itemStack, int enchantLevel) {
        addPotionEffect(player, PotionEffectType.FAST_DIGGING, enchantLevel-1)
    }

    @Override
    void onUnequip(Player player, ItemStack itemStack, int enchantLevel) {
        removePotionEffect(player, PotionEffectType.FAST_DIGGING, enchantLevel-1)
    }
}
