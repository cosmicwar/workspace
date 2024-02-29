package scripts.factions.features.enchant.enchants.ultimate

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.shared.utils.ItemType

class Obsidianshield extends CustomEnchantment {

    Obsidianshield() {
        super(
                "obsidianshield",
                EnchantmentTier.ULTIMATE,
                EnchantmentType.NORMAL,
                "Obsidian Shield",
                ["Gives you fire resistance for 5 seconds per level when you are hit."],
                [ItemType.HELMET, ItemType.CHESTPLATE, ItemType.LEGGINGS, ItemType.BOOTS],
                1
        )
    }

    @Override
    void onEquip(Player player, ItemStack itemStack, int enchantLevel) {
        addPotionEffect(player, PotionEffectType.FIRE_RESISTANCE, enchantLevel - 1)
    }

    @Override
    void onUnequip(Player player, ItemStack itemStack, int enchantLevel) {
        removePotionEffect(player, PotionEffectType.FIRE_RESISTANCE, enchantLevel - 1)
    }
}
