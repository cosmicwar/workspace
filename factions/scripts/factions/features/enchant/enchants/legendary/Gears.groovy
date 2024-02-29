package scripts.factions.features.enchant.enchants.legendary

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.shared.utils.ItemType

class Gears extends CustomEnchantment {

    Gears() {
        super(
                "gears",
                EnchantmentTier.LEGENDARY,
                EnchantmentType.NORMAL,
                "Gears",
                ["Increases your movement speed."],
                [ItemType.BOOTS],
                3
        )
    }

    @Override
    void onEquip(Player player, ItemStack itemStack, int enchantLevel) {
        addPotionEffect(player, PotionEffectType.SPEED, enchantLevel - 1)
    }

    @Override
    void onUnequip(Player player, ItemStack itemStack, int enchantLevel) {
        removePotionEffect(player, PotionEffectType.SPEED, enchantLevel - 1)
    }
}
