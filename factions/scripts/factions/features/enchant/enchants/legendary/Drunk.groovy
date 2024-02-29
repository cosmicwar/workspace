package scripts.factions.features.enchant.enchants.legendary

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.shared.utils.ItemType

class Drunk extends CustomEnchantment {

    Drunk() {
        super(
                "drunk",
                EnchantmentTier.LEGENDARY,
                EnchantmentType.NORMAL,
                "Drunk",
                ["Increases damage dealt, but slows you down."],
                [ItemType.HELMET],
                4
        )
    }

    @Override
    void onEquip(Player player, ItemStack itemStack, int enchantLevel) {
        addPotionEffect(player, PotionEffectType.INCREASE_DAMAGE, enchantLevel / 2 as int)
        addPotionEffect(player, PotionEffectType.SLOW, Math.max(enchantLevel - 2, 0))
        addPotionEffect(player, PotionEffectType.SLOW_DIGGING, enchantLevel / 2 as int)
    }

    @Override
    void onUnequip(Player player, ItemStack itemStack, int enchantLevel) {
        removePotionEffect(player, PotionEffectType.INCREASE_DAMAGE, enchantLevel / 2 as int)
        removePotionEffect(player, PotionEffectType.SLOW, Math.max(enchantLevel - 2, 0))
        removePotionEffect(player, PotionEffectType.SLOW_DIGGING, enchantLevel / 2 as int)
    }
}
