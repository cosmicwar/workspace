package scripts.factions.features.enchant.enchants.heroic

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.struct.HeroicEnchant
import scripts.shared.utils.ItemType

class GodlyOverload extends HeroicEnchant {

    GodlyOverload() {
        super(
                "godly_overload",
                EnchantmentType.NORMAL,
                "Godly Overload",
                "overload",
                ["Increases your maximum health."],
                [ItemType.HELMET, ItemType.CHESTPLATE, ItemType.LEGGINGS, ItemType.BOOTS],
                3
        )
    }

    @Override
    void onEquip(Player player, ItemStack itemStack, int enchantLevel) {
        addPotionEffect(player, PotionEffectType.HEALTH_BOOST, (enchantLevel * 2) - 1)

    }

    @Override
    void onUnequip(Player player, ItemStack itemStack, int enchantLevel) {
        removePotionEffect(player, PotionEffectType.HEALTH_BOOST, (enchantLevel * 2) - 1)
    }
}
